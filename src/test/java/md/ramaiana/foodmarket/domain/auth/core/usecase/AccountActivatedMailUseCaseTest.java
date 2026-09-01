package md.ramaiana.foodmarket.domain.auth.core.usecase;

import md.ramaiana.foodmarket.config.RegistrationProperties;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.email.core.exception.MailException;
import md.ramaiana.foodmarket.domain.email.core.request.AccountActivatedVariables;
import md.ramaiana.foodmarket.domain.email.core.request.EmailSendRequest;
import md.ramaiana.foodmarket.domain.email.core.response.EmailSendResponse;
import md.ramaiana.foodmarket.domain.email.core.usecase.EmailSendUseCase;
import md.ramaiana.foodmarket.shared.enums.Language;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AccountActivatedMailUseCaseTest {

  @Mock
  EmailSendUseCase emailSendUseCase;

  private AccountActivatedMailUseCase useCase;

  @BeforeEach
  void setUp() {
    RegistrationProperties registrationProperties = new RegistrationProperties(
        "https://app.example.com/confirmEmail", 24, 60, "https://app.example.com/signIn");
    useCase = new AccountActivatedMailUseCase(emailSendUseCase, registrationProperties);
  }

  private static AppUserEntity user(Language language) {
    return new AppUserEntity(1, "user@example.com", "hash", null, null,
        Instant.now(), UserState.ACTIVE, language, new HashSet<>(), null);
  }

  @Test
  void execute_should_send_in_the_recipients_own_language() {
    when(emailSendUseCase.execute(any())).thenReturn(new EmailSendResponse("uuid", "user@example.com"));

    boolean sent = useCase.execute(user(Language.RO));

    assertThat(sent).isTrue();
    ArgumentCaptor<EmailSendRequest> captor = ArgumentCaptor.forClass(EmailSendRequest.class);
    verify(emailSendUseCase).execute(captor.capture());

    AccountActivatedVariables variables = (AccountActivatedVariables) captor.getValue().variables();
    assertThat(variables.language()).isEqualTo(Language.RO);
    assertThat(variables.loginUrl()).isEqualTo("https://app.example.com/signIn");
    assertThat(captor.getValue().recipient().email()).isEqualTo("user@example.com");
  }

  @Test
  void execute_should_swallow_mail_exception_and_return_false() {
    when(emailSendUseCase.execute(any())).thenThrow(new MailException("Mailjet is down"));

    boolean sent = useCase.execute(user(Language.RU));

    assertThat(sent).isFalse();
  }
}
