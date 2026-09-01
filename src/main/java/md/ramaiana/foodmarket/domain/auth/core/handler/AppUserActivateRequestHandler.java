package md.ramaiana.foodmarket.domain.auth.core.handler;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.response.AppUserResponse;
import md.ramaiana.foodmarket.domain.auth.core.usecase.AppUserActivateUseCase;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.shared.annotation.RequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

/**
 * Request handler for an administrator activating a confirmed registration.
 */
@RequestHandler
@RequiredArgsConstructor
public class AppUserActivateRequestHandler {

  // Use cases
  private final AppUserActivateUseCase appUserActivateUseCase;

  // Repositories
  private final ClientRepository clientRepository;

  // Proxies
  @Lazy @Autowired private AppUserActivateRequestHandler thisProxy;

  /**
   * Handle the request.
   */
  @NonNull
  public AppUserResponse handle(@NonNull Integer userId) {
    AppUserEntity activatedUser = thisProxy.persist(userId);

    appUserActivateUseCase.executeSideEffects(activatedUser);

    return new AppUserResponse(activatedUser, clientNameOf(activatedUser));
  }

  @NonNull
  @Transactional(readOnly = false, rollbackFor = Exception.class)
  protected AppUserEntity persist(@NonNull Integer userId) {
    return appUserActivateUseCase.executeTransactionalEffect(userId);
  }

  @Nullable
  private String clientNameOf(@NonNull AppUserEntity user) {
    if (!user.hasClient()) {
      return null;
    }
    return clientRepository.findById(user.getClient().getId())
        .map(ClientEntity::getName)
        .orElse(null);
  }
}
