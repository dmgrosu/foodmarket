package md.ramaiana.foodmarket.domain.auth.core.usecase;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import md.ramaiana.foodmarket.config.RegistrationProperties;
import md.ramaiana.foodmarket.domain.auth.core.request.RegistrationConfirmationResendRequest;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationTokenIssueUseCase.TransactionalEffectResult;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenRepository;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RegistrationConfirmationResendUseCaseTest {

    @Mock
    AppUserFindByEmailUseCase appUserFindByEmailUseCase;
    @Mock
    RegistrationTokenRepository registrationTokenRepository;
    @Mock
    RegistrationTokenIssueUseCase registrationTokenIssueUseCase;
    @Mock
    RegistrationConfirmationMailUseCase registrationConfirmationMailUseCase;

    private static AppUserEntity user(UserState state) {
        return new AppUserEntity(1, "user@example.com", "hash",
            Instant.now(), state, Language.RU, new HashSet<>(), null);
    }

    private RegistrationConfirmationResendUseCase useCase(int cooldownSeconds) {
        RegistrationProperties properties = new RegistrationProperties(
            "https://app.example.com/confirmEmail", 24, cooldownSeconds, "https://app.example.com/signIn");
        return new RegistrationConfirmationResendUseCase(
            appUserFindByEmailUseCase, registrationTokenRepository,
            registrationTokenIssueUseCase, registrationConfirmationMailUseCase, properties);
    }

    @Test
    void executeTransactionalEffect_should_reissue_a_token_when_no_prior_token_exists() {
        when(appUserFindByEmailUseCase.execute("user@example.com"))
            .thenReturn(user(UserState.PENDING_CONFIRMATION));
        when(registrationTokenRepository.findLatestForUser(1)).thenReturn(Optional.empty());
        TransactionalEffectResult pending = new TransactionalEffectResult(user(UserState.PENDING_CONFIRMATION), "url", 24);
        when(registrationTokenIssueUseCase.execute(any())).thenReturn(pending);

        TransactionalEffectResult result = useCase(60).executeTransactionalEffect(
            new RegistrationConfirmationResendRequest("user@example.com"));

        assertThat(result).isEqualTo(pending);
    }

    @Test
    void executeTransactionalEffect_should_reissue_a_token_once_the_cooldown_has_elapsed() {
        when(appUserFindByEmailUseCase.execute("user@example.com"))
            .thenReturn(user(UserState.PENDING_CONFIRMATION));
        RegistrationTokenEntity oldToken = new RegistrationTokenEntity(
            null, AggregateReference.to(1), "old-hash", Instant.now().plusSeconds(3600), null,
            Instant.now().minusSeconds(120));
        when(registrationTokenRepository.findLatestForUser(1)).thenReturn(Optional.of(oldToken));
        TransactionalEffectResult pending = new TransactionalEffectResult(user(UserState.PENDING_CONFIRMATION), "url", 24);
        when(registrationTokenIssueUseCase.execute(any())).thenReturn(pending);

        // Cooldown of 1s, last token was created 120s ago — well past the cooldown.
        TransactionalEffectResult result = useCase(1).executeTransactionalEffect(
            new RegistrationConfirmationResendRequest("user@example.com"));

        assertThat(result).isEqualTo(pending);
    }

    @Test
    void executeTransactionalEffect_should_reject_when_inside_the_cooldown() {
        when(appUserFindByEmailUseCase.execute("user@example.com"))
            .thenReturn(user(UserState.PENDING_CONFIRMATION));
        RegistrationTokenEntity recentToken = new RegistrationTokenEntity(
            AggregateReference.to(1), "recent-hash", Instant.now().plusSeconds(3600));
        when(registrationTokenRepository.findLatestForUser(1)).thenReturn(Optional.of(recentToken));

        assertThatThrownBy(() -> useCase(60).executeTransactionalEffect(
            new RegistrationConfirmationResendRequest("user@example.com")))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void executeTransactionalEffect_should_reject_a_non_pending_user() {
        when(appUserFindByEmailUseCase.execute("user@example.com"))
            .thenReturn(user(UserState.ACTIVE));

        assertThatThrownBy(() -> useCase(60).executeTransactionalEffect(
            new RegistrationConfirmationResendRequest("user@example.com")))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void executeTransactionalEffect_should_propagate_unknown_email_as_not_found() {
        when(appUserFindByEmailUseCase.execute("missing@example.com"))
            .thenThrow(new NotFoundException("not found"));

        assertThatThrownBy(() -> useCase(60).executeTransactionalEffect(
            new RegistrationConfirmationResendRequest("missing@example.com")))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void executeSideEffects_should_delegate_to_the_mail_use_case() {
        TransactionalEffectResult pending = new TransactionalEffectResult(user(UserState.PENDING_CONFIRMATION), "url", 24);
        when(registrationConfirmationMailUseCase.execute(pending)).thenReturn(true);

        boolean sent = useCase(60).executeSideEffects(pending);

        assertThat(sent).isTrue();
        verify(registrationConfirmationMailUseCase).execute(pending);
    }
}
