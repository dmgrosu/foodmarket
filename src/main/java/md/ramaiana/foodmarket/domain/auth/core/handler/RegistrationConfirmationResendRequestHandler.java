package md.ramaiana.foodmarket.domain.auth.core.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.request.RegistrationConfirmationResendRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.RegistrationResponse;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationConfirmationResendUseCase;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationTokenIssueUseCase.TransactionalEffectResult;
import md.ramaiana.foodmarket.shared.annotation.RequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

/**
 * Request handler for resending a registration confirmation email.
 */
@RequestHandler
@RequiredArgsConstructor
public class RegistrationConfirmationResendRequestHandler {

  // Use cases
  private final RegistrationConfirmationResendUseCase registrationConfirmationResendUseCase;

  // Proxies
  @Lazy @Autowired private RegistrationConfirmationResendRequestHandler thisProxy;

  /**
   * Handle the request.
   */
  @NonNull
  public RegistrationResponse handle(@NonNull RegistrationConfirmationResendRequest request) {
    TransactionalEffectResult transactionalEffectResult = thisProxy.persist(request);

    boolean confirmationEmailSent = registrationConfirmationResendUseCase.executeSideEffects(transactionalEffectResult);

    return new RegistrationResponse(transactionalEffectResult.user(), confirmationEmailSent);
  }

  @NonNull
  @Transactional(readOnly = false, rollbackFor = Exception.class)
  protected TransactionalEffectResult persist(@NonNull RegistrationConfirmationResendRequest request) {
    return registrationConfirmationResendUseCase.executeTransactionalEffect(request);
  }
}
