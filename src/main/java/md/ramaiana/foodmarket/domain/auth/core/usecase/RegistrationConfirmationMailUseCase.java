package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationTokenIssueUseCase.TransactionalEffectResult;
import md.ramaiana.foodmarket.domain.email.core.exception.MailException;
import md.ramaiana.foodmarket.domain.email.core.request.EmailRecipient;
import md.ramaiana.foodmarket.domain.email.core.request.EmailSendRequest;
import md.ramaiana.foodmarket.domain.email.core.request.RegistrationConfirmationVariables;
import md.ramaiana.foodmarket.domain.email.core.usecase.EmailSendUseCase;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for emailing a registration confirmation link. The external-call half of registration
 * confirmation: never runs inside a transaction, and never lets a Mailjet failure become the caller's
 * problem — it is reported back as a boolean so the caller can commit regardless and let the user
 * resend later.
 */
@Slf4j
@UseCase
@RequiredArgsConstructor
public class RegistrationConfirmationMailUseCase {

  private final EmailSendUseCase emailSendUseCase;

  /**
   * Execute the use case.
   *
   * @return {@code true} if the email was sent, {@code false} if sending failed.
   */
  @Transactional(propagation = Propagation.NEVER)
  public boolean execute(@NonNull TransactionalEffectResult transactionalEffectResult) {
    EmailRecipient recipient = new EmailRecipient(transactionalEffectResult.user().getEmail(), null);
    RegistrationConfirmationVariables variables = new RegistrationConfirmationVariables(
        transactionalEffectResult.confirmationUrl(),
        transactionalEffectResult.expiresInHours(),
        // The recipient's own language — not the language of whoever triggered the send, which
        // matters once an administrator can trigger a resend on someone else's behalf.
        transactionalEffectResult.user().getLanguage()
    );

    try {
      emailSendUseCase.execute(new EmailSendRequest(recipient, variables));
      return true;
    } catch (MailException e) {
      log.error("Failed to send registration confirmation email to {}", recipient.email(), e);
      return false;
    }
  }
}
