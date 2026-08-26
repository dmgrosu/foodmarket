package md.ramaiana.foodmarket.domain.email.core.usecase;

import md.ramaiana.foodmarket.domain.email.core.exception.MailException;
import md.ramaiana.foodmarket.domain.email.core.request.EmailRecipient;
import md.ramaiana.foodmarket.domain.email.core.request.EmailSendRequest;
import md.ramaiana.foodmarket.domain.email.core.response.EmailSendResponse;
import md.ramaiana.foodmarket.domain.email.data.MailjetAdapter;
import md.ramaiana.foodmarket.shared.enums.Language;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class EmailSendUseCaseTest {

    @Mock
    MailjetAdapter mailjetAdapter;

    @InjectMocks
    EmailSendUseCase useCase;

    @Test
    void execute_should_delegate_to_adapter_and_return_result() {
        // Arrange
        EmailRecipient recipient = new EmailRecipient("user@example.com", "User Name");
        var variables = new md.ramaiana.foodmarket.domain.email.core.request.RegistrationConfirmationVariables(
            "https://app.example.com/confirmEmail?confirmationToken=abc123",
            24,
            Language.RU
        );
        EmailSendRequest request = new EmailSendRequest(recipient, variables);
        EmailSendResponse expectedResult = new EmailSendResponse("msg-uuid-123", recipient.email());

        when(mailjetAdapter.send(recipient, variables)).thenReturn(expectedResult);

        // Act
        EmailSendResponse result = useCase.execute(request);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(mailjetAdapter).send(recipient, variables);
    }

    @Test
    void execute_should_propagate_mail_send_exception() {
        // Arrange
        EmailRecipient recipient = new EmailRecipient("user@example.com", "User Name");
        var variables = new md.ramaiana.foodmarket.domain.email.core.request.RegistrationConfirmationVariables(
            "https://app.example.com/confirmEmail?confirmationToken=abc123",
            24,
            Language.RU
        );
        EmailSendRequest request = new EmailSendRequest(recipient, variables);

        when(mailjetAdapter.send(recipient, variables))
            .thenThrow(new MailException("Mailjet returned error"));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(request))
            .isInstanceOf(MailException.class)
            .hasMessageContaining("Mailjet returned error");
    }
}
