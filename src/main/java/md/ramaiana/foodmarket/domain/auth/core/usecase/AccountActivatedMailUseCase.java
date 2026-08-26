package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.config.RegistrationProperties;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.email.core.exception.MailException;
import md.ramaiana.foodmarket.domain.email.core.request.AccountActivatedVariables;
import md.ramaiana.foodmarket.domain.email.core.request.EmailRecipient;
import md.ramaiana.foodmarket.domain.email.core.request.EmailSendRequest;
import md.ramaiana.foodmarket.domain.email.core.usecase.EmailSendUseCase;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for emailing a user once an administrator activates their account. The external-call half
 * of activation: never runs inside a transaction, and never lets a Mailjet failure undo an activation
 * that is already committed — it is reported back as a boolean instead.
 */
@Slf4j
@UseCase
@RequiredArgsConstructor
@EnableConfigurationProperties(RegistrationProperties.class)
public class AccountActivatedMailUseCase {

  private final EmailSendUseCase emailSendUseCase;
  private final RegistrationProperties registrationProperties;

  /**
   * Execute the use case.
   *
   * @return {@code true} if the email was sent, {@code false} if sending failed.
   */
  @Transactional(propagation = Propagation.NEVER)
  public boolean execute(@NonNull AppUserEntity user) {
    EmailRecipient recipient = new EmailRecipient(user.getEmail(), null);
    AccountActivatedVariables variables = new AccountActivatedVariables(
        registrationProperties.loginPageUrl(),
        // The recipient's own language, not the administrator's.
        user.getLanguage()
    );

    try {
      emailSendUseCase.execute(new EmailSendRequest(recipient, variables));
      return true;
    } catch (MailException e) {
      log.error("Failed to send account activated email to {}", recipient.email(), e);
      return false;
    }
  }
}
