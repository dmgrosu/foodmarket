package md.ramaiana.foodmarket.domain.auth.core.usecase;

import md.ramaiana.foodmarket.config.RegistrationProperties;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationTokenIssueUseCase.TransactionalEffectResult;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenRepository;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.util.SecureTokenGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RegistrationTokenIssueUseCaseTest {

    @Mock
    RegistrationTokenRepository registrationTokenRepository;

    // Real generator, not a mock — RegistrationTokenIssueUseCase and
    // RegistrationConfirmUseCase must agree on hashing, so this and the confirm-side test
    // both exercise the real algorithm rather than two mocks that could silently disagree.
    @Spy
    SecureTokenGenerator secureTokenGenerator;

    @Captor
    ArgumentCaptor<RegistrationTokenEntity> tokenCaptor;

    @Test
    void execute_should_expire_prior_tokens_issue_a_new_one_and_build_the_confirmation_url() {
        RegistrationProperties properties = new RegistrationProperties(
            "https://app.example.com/confirmEmail", 24, 60, "https://app.example.com/signIn");
        RegistrationTokenIssueUseCase useCase = new RegistrationTokenIssueUseCase(
            registrationTokenRepository, secureTokenGenerator, properties);
        AppUserEntity user = new AppUserEntity(1, "user@example.com", "hash", null, null,
            java.time.Instant.now(), UserState.PENDING_CONFIRMATION, Language.RU, new java.util.HashSet<>(), null);
        when(registrationTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionalEffectResult result = useCase.execute(user);

        verify(registrationTokenRepository).expireLiveTokensForUser(eq(1), any());
        verify(registrationTokenRepository).save(tokenCaptor.capture());

        RegistrationTokenEntity savedToken = tokenCaptor.getValue();
        String confirmationToken = extractToken(result.confirmationUrl());

        // The persisted hash must never equal the raw token that goes into the emailed link.
        assertThat(savedToken.getTokenHash()).isNotEqualTo(confirmationToken);
        assertThat(savedToken.getTokenHash()).isEqualTo(secureTokenGenerator.hash(confirmationToken));

        assertThat(result.confirmationUrl()).startsWith("https://app.example.com/confirmEmail?confirmationToken=");
        assertThat(result.expiresInHours()).isEqualTo(24);
        assertThat(result.user()).isEqualTo(user);
    }

    private static String extractToken(String url) {
        String marker = "confirmationToken=";
        int idx = url.indexOf(marker);
        return url.substring(idx + marker.length());
    }
}
