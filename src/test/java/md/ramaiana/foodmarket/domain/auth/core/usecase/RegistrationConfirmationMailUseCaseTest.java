package md.ramaiana.foodmarket.domain.auth.core.usecase;

import java.time.Instant;
import java.util.HashSet;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationTokenIssueUseCase.TransactionalEffectResult;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.email.core.exception.MailException;
import md.ramaiana.foodmarket.domain.email.core.request.EmailSendRequest;
import md.ramaiana.foodmarket.domain.email.core.request.RegistrationConfirmationVariables;
import md.ramaiana.foodmarket.domain.email.core.response.EmailSendResponse;
import md.ramaiana.foodmarket.domain.email.core.usecase.EmailSendUseCase;
import md.ramaiana.foodmarket.shared.enums.EmailTemplate;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RegistrationConfirmationMailUseCaseTest {

    @Mock
    EmailSendUseCase emailSendUseCase;

    @InjectMocks
    RegistrationConfirmationMailUseCase useCase;

    @Captor
    ArgumentCaptor<EmailSendRequest> requestCaptor;

    private static AppUserEntity user(Language language) {
        return new AppUserEntity(1, "user@example.com", "hash", null, null,
            Instant.now(), UserState.PENDING_CONFIRMATION, language, new HashSet<>(), null);
    }

    private static AppUserEntity user() {
        return new AppUserEntity(1, "user@example.com", "hash", null, null,
            Instant.now(), UserState.PENDING_CONFIRMATION, Language.RU, new HashSet<>(), null);
    }

    @Test
    void execute_should_send_the_correct_template_and_variables_and_return_true() {
        TransactionalEffectResult pending = new TransactionalEffectResult(
            user(), "https://app.example.com/confirmEmail?confirmationToken=abc", 24);
        when(emailSendUseCase.execute(any())).thenReturn(new EmailSendResponse("uuid", "user@example.com"));

        boolean sent = useCase.execute(pending);

        assertThat(sent).isTrue();
        verify(emailSendUseCase).execute(requestCaptor.capture());
        EmailSendRequest request = requestCaptor.getValue();
        assertThat(request.recipient().email()).isEqualTo("user@example.com");
        assertThat(request.variables()).isInstanceOf(RegistrationConfirmationVariables.class);
        RegistrationConfirmationVariables variables = (RegistrationConfirmationVariables) request.variables();
        assertThat(variables.confirmationUrl()).isEqualTo(pending.confirmationUrl());
        assertThat(variables.expiresInHours()).isEqualTo(24);
    }

    @Test
    void execute_should_send_in_the_recipients_own_language() {
        // Not the language of whoever triggered the send — this is what makes an admin-triggered
        // resend still reach the user in their language.
        TransactionalEffectResult pending = new TransactionalEffectResult(
            user(Language.RO), "https://app.example.com/confirmEmail?confirmationToken=abc", 24);
        when(emailSendUseCase.execute(any())).thenReturn(new EmailSendResponse("uuid", "user@example.com"));

        useCase.execute(pending);

        verify(emailSendUseCase).execute(requestCaptor.capture());
        RegistrationConfirmationVariables variables =
            (RegistrationConfirmationVariables) requestCaptor.getValue().variables();
        assertThat(variables.language()).isEqualTo(Language.RO);
        // Language selects which of the three imported templates is sent; it is deliberately not a
        // rendered variable, so the copy is never branched at send time.
        assertThat(variables.variables()).doesNotContainKey("language");
        assertThat(EmailTemplate.REGISTRATION_CONFIRMATION.getId(variables.language()))
            .isEqualTo(EmailTemplate.REGISTRATION_CONFIRMATION.getId(Language.RO));
    }

    @Test
    void execute_should_swallow_mail_exception_and_return_false() {
        TransactionalEffectResult pending = new TransactionalEffectResult(
            user(), "https://app.example.com/confirmEmail?confirmationToken=abc", 24);
        when(emailSendUseCase.execute(any())).thenThrow(new MailException("Mailjet is down"));

        boolean sent = useCase.execute(pending);

        assertThat(sent).isFalse();
    }
}
