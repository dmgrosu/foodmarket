package md.ramaiana.foodmarket.domain.auth.core.usecase;

import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.Role;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.exception.http.ForbiddenException;
import md.ramaiana.foodmarket.shared.exception.http.UnauthorizedException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class JwtGetAuthenticationUseCaseTest {

    private static final String TOKEN = "a.signed.token";

    @Mock
    JwtVerifyTokenUseCase jwtVerifyTokenUseCase;
    @Mock
    AppUserFindByIdUseCase appUserFindByIdUseCase;

    @InjectMocks
    JwtGetAuthenticationUseCase useCase;

    private static AppUserEntity user(UserState state) {
        AppUserEntity user = new AppUserEntity(1, "user@example.com", "hash",
            Instant.now(), state, Language.RU, new HashSet<>(), null);
        user.addRole(Role.USER);
        return user;
    }

    private void tokenResolvesToUserIn(UserState state) {
        when(jwtVerifyTokenUseCase.execute(TOKEN)).thenReturn(Map.of("id", "1", "email", "user@example.com"));
        when(appUserFindByIdUseCase.execute(1)).thenReturn(user(state));
    }

    @Test
    void execute_should_authenticate_an_active_user() {
        tokenResolvesToUserIn(UserState.ACTIVE);

        Authentication authentication = useCase.execute(TOKEN);

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(AppUserEntity.class);
        assertThat(authentication.getAuthorities()).extracting(Object::toString).contains("ROLE_USER");
    }

    @ParameterizedTest
    @EnumSource(value = UserState.class, names = "ACTIVE", mode = EnumSource.Mode.EXCLUDE)
    void execute_should_reject_every_state_other_than_active(UserState state) {
        // A JWT outlives the account it was issued for. Without this check a suspended user would
        // keep full access until expiry, and the token issued at email confirmation would let an
        // unapproved user past every authenticated() endpoint.
        tokenResolvesToUserIn(state);

        assertThatThrownBy(() -> useCase.execute(TOKEN))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("not active");
    }

    @Test
    void execute_should_reject_a_token_that_fails_verification() {
        // JwtVerifyTokenUseCase returns null rather than throwing on a bad signature or expiry.
        when(jwtVerifyTokenUseCase.execute(TOKEN)).thenReturn(null);

        assertThatThrownBy(() -> useCase.execute(TOKEN))
            .isInstanceOf(UnauthorizedException.class);
    }
}
