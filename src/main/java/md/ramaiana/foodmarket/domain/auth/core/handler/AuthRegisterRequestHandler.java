package md.ramaiana.foodmarket.domain.auth.core.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.request.RegisterRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.RegistrationResponse;
import md.ramaiana.foodmarket.domain.auth.core.usecase.AuthRegisterUseCase;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationTokenIssueUseCase.TransactionalEffectResult;
import md.ramaiana.foodmarket.shared.annotation.RequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

/**
 * Request handler for user registration.
 */
@RequestHandler
@RequiredArgsConstructor
public class AuthRegisterRequestHandler {

  // Use cases
  private final AuthRegisterUseCase authRegisterUseCase;

  // Proxies
  @Lazy @Autowired private AuthRegisterRequestHandler thisProxy;

  /**
   * Handle the request.
   */
  @NonNull
  public RegistrationResponse handle(@NonNull RegisterRequest request) {
    authRegisterUseCase.preExecute(request);

    TransactionalEffectResult transactionalEffectResult = thisProxy.persist(request);

    boolean confirmationEmailSent = authRegisterUseCase.executeSideEffects(transactionalEffectResult);

    return new RegistrationResponse(transactionalEffectResult.user(), confirmationEmailSent);
  }

  @NonNull
  @Transactional(readOnly = false, rollbackFor = Exception.class)
  protected TransactionalEffectResult persist(@NonNull RegisterRequest request) {
    return authRegisterUseCase.executeTransactionalEffect(request);
  }
}
