package md.ramaiana.foodmarket.domain.auth.core.usecase;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import md.ramaiana.foodmarket.domain.auth.core.request.RegistrationConfirmRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.RegistrationConfirmResponse;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenRepository;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.util.SecureTokenGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RegistrationConfirmUseCaseTest {

    @Mock
    RegistrationTokenRepository registrationTokenRepository;
    @Mock
    AppUserRepository appUserRepository;
    @Mock
    AppUserFindByIdUseCase appUserFindByIdUseCase;

    // Real generator so the hash this test looks up matches what
    // RegistrationTokenIssueUseCase would actually have persisted.
    @Spy
    SecureTokenGenerator secureTokenGenerator;

    @Mock
    JwtCreateTokenUseCase jwtCreateTokenUseCase;

    private RegistrationConfirmUseCase useCase;

    private static final String RAW_TOKEN = "raw-confirmation-token";

    private RegistrationConfirmUseCase newUseCase() {
        // A session is minted on every successful path, including the idempotent replays.
        lenient().when(jwtCreateTokenUseCase.execute(any())).thenReturn("Bearer signed-token");
        lenient().when(jwtCreateTokenUseCase.getTokenValidityInSeconds()).thenReturn(3600);
        return new RegistrationConfirmUseCase(
            registrationTokenRepository, appUserRepository, appUserFindByIdUseCase,
            secureTokenGenerator, jwtCreateTokenUseCase);
    }

    private static AppUserEntity user(UserState state) {
        return new AppUserEntity(1, "user@example.com", "hash", null, null,
            Instant.now(), state, Language.RU, new HashSet<>(), null);
    }

    private RegistrationTokenEntity liveToken() {
        return new RegistrationTokenEntity(
            AggregateReference.to(1), secureTokenGenerator.hash(RAW_TOKEN), Instant.now().plusSeconds(3600));
    }

    @Test
    void execute_should_confirm_a_pending_user() {
        useCase = newUseCase();
        RegistrationTokenEntity token = liveToken();
        when(registrationTokenRepository.findByTokenHash(secureTokenGenerator.hash(RAW_TOKEN)))
            .thenReturn(Optional.of(token));
        when(appUserFindByIdUseCase.execute(1)).thenReturn(user(UserState.PENDING_CONFIRMATION));
        when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistrationConfirmResponse result = useCase.execute(new RegistrationConfirmRequest(RAW_TOKEN));

        assertThat(result.state()).isEqualTo(UserState.CONFIRMED);
        verify(registrationTokenRepository).save(argThat(RegistrationTokenEntity::isConfirmed));
    }

    @Test
    void execute_should_be_idempotent_for_an_already_confirmed_user() {
        useCase = newUseCase();
        RegistrationTokenEntity token = liveToken();
        when(registrationTokenRepository.findByTokenHash(secureTokenGenerator.hash(RAW_TOKEN)))
            .thenReturn(Optional.of(token));
        when(appUserFindByIdUseCase.execute(1)).thenReturn(user(UserState.CONFIRMED));

        RegistrationConfirmResponse result = useCase.execute(new RegistrationConfirmRequest(RAW_TOKEN));

        assertThat(result.state()).isEqualTo(UserState.CONFIRMED);
        verify(registrationTokenRepository, never()).save(any());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void execute_should_be_idempotent_for_an_active_user() {
        useCase = newUseCase();
        RegistrationTokenEntity token = liveToken();
        when(registrationTokenRepository.findByTokenHash(secureTokenGenerator.hash(RAW_TOKEN)))
            .thenReturn(Optional.of(token));
        when(appUserFindByIdUseCase.execute(1)).thenReturn(user(UserState.ACTIVE));

        RegistrationConfirmResponse result = useCase.execute(new RegistrationConfirmRequest(RAW_TOKEN));

        assertThat(result.state()).isEqualTo(UserState.ACTIVE);
        verify(registrationTokenRepository, never()).save(any());
    }

    @Test
    void execute_should_return_a_session_alongside_the_confirmed_user() {
        // The token is inert while the user is only CONFIRMED — JwtGetAuthenticationUseCase rejects
        // a non-ACTIVE user — but it is handed over so the browser need not re-ask for credentials.
        useCase = newUseCase();
        RegistrationTokenEntity token = liveToken();
        when(registrationTokenRepository.findByTokenHash(secureTokenGenerator.hash(RAW_TOKEN)))
            .thenReturn(Optional.of(token));
        when(appUserFindByIdUseCase.execute(1)).thenReturn(user(UserState.PENDING_CONFIRMATION));
        when(appUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistrationConfirmResponse result = useCase.execute(new RegistrationConfirmRequest(RAW_TOKEN));

        assertThat(result.token()).isEqualTo("Bearer signed-token");
        assertThat(result.tokenTtl()).isEqualTo(3600);
    }

    @Test
    void execute_should_reject_an_unknown_token() {
        useCase = newUseCase();
        when(registrationTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new RegistrationConfirmRequest("unknown-token")))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void execute_should_reject_an_expired_token_while_still_pending() {
        useCase = newUseCase();
        RegistrationTokenEntity expired = new RegistrationTokenEntity(
            AggregateReference.to(1), secureTokenGenerator.hash(RAW_TOKEN),
            Instant.now().minus(1, ChronoUnit.HOURS));
        when(registrationTokenRepository.findByTokenHash(secureTokenGenerator.hash(RAW_TOKEN)))
            .thenReturn(Optional.of(expired));
        when(appUserFindByIdUseCase.execute(1)).thenReturn(user(UserState.PENDING_CONFIRMATION));

        assertThatThrownBy(() -> useCase.execute(new RegistrationConfirmRequest(RAW_TOKEN)))
            .isInstanceOf(BadRequestException.class);
    }
}
