package md.ramaiana.foodmarket.domain.auth.core.handler;

import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.request.PasswordResetInitiateRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.PasswordResetInitiateResponse;
import md.ramaiana.foodmarket.domain.auth.core.usecase.PasswordResetInitiateUseCase;
import md.ramaiana.foodmarket.domain.auth.core.usecase.PasswordResetTokenIssueUseCase.TransactionalEffectResult;
import md.ramaiana.foodmarket.shared.annotation.RequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

/**
 * Request handler for issuing a password reset link: the token write commits before the email goes
 * out, so a Mailjet failure never rolls back a token the user may already have received.
 */
@RequestHandler
@RequiredArgsConstructor
public class PasswordResetInitiateRequestHandler {

  // Use cases
  private final PasswordResetInitiateUseCase passwordResetInitiateUseCase;

  // Proxies
  @Lazy @Autowired private PasswordResetInitiateRequestHandler thisProxy;

  /**
   * Handle the request.
   */
  @NonNull
  public PasswordResetInitiateResponse handle(@NonNull PasswordResetInitiateRequest request) {
    Optional<TransactionalEffectResult> transactionalEffectResult = thisProxy.persist(request);

    // Empty means the request was silently declined — see PasswordResetInitiateUseCase. The response
    // must not differ from the success case, so report sent and send nothing.
    boolean resetEmailSent = transactionalEffectResult
        .map(passwordResetInitiateUseCase::executeSideEffects)
        .orElse(true);

    return new PasswordResetInitiateResponse(request.email(), resetEmailSent);
  }

  @NonNull
  @Transactional(readOnly = false, rollbackFor = Exception.class)
  protected Optional<TransactionalEffectResult> persist(@NonNull PasswordResetInitiateRequest request) {
    return passwordResetInitiateUseCase.executeTransactionalEffect(request);
  }
}
