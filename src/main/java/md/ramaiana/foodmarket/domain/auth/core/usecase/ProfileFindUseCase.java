package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.response.ProfileResponse;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.domain.client.core.usecase.ClientFindByIdUseCase;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for reading the signed-in user's own profile.
 * <p>
 * The caller is the identity {@code JwtGetAuthenticationUseCase} put in the security context, which
 * re-reads the row on every request — so it is already current and needs no reload here.
 */
@UseCase
@RequiredArgsConstructor
public class ProfileFindUseCase {

  private final ClientFindByIdUseCase clientFindByIdUseCase;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public ProfileResponse execute(@NonNull AppUserEntity currentUser) {
    ClientResponse clientResponse = null;
    if (currentUser.hasClient()) {
      ClientEntity client = clientFindByIdUseCase.execute(currentUser.getClient());
      clientResponse = new ClientResponse(client);
    }
    return new ProfileResponse(currentUser, clientResponse);
  }
}
