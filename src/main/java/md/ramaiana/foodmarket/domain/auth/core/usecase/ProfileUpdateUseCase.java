package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.request.ProfileUpdateRequest;
import md.ramaiana.foodmarket.domain.auth.core.response.ProfileResponse;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.client.core.response.ClientResponse;
import md.ramaiana.foodmarket.domain.client.core.usecase.ClientFindByIdUseCase;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.enums.Language;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for updating the signed-in user's own profile: their name and their interface language.
 */
@UseCase
@RequiredArgsConstructor
public class ProfileUpdateUseCase {

  private final AppUserRepository appUserRepository;
  private final AppUserFindByIdUseCase appUserFindByIdUseCase;
  private final ClientFindByIdUseCase clientFindByIdUseCase;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(rollbackFor = Exception.class)
  public ProfileResponse execute(@NonNull AppUserEntity currentUser, @NonNull ProfileUpdateRequest request) {
    // Reload inside the transaction: the principal was read by the JWT filter, before this write began.
    AppUserEntity user = appUserFindByIdUseCase.execute(currentUser.getId());

    AppUserEntity updated = appUserRepository.save(user.withProfile(
        normalize(request.firstName()),
        normalize(request.lastName()),
        Language.fromTag(request.language())
    ));

    ClientResponse clientResponse = null;
    if (updated.hasClient()) {
      ClientEntity client = clientFindByIdUseCase.execute(updated.getClient());
      clientResponse = new ClientResponse(client);
    }
    return new ProfileResponse(updated, clientResponse);
  }

  /**
   * An omitted name and a name of nothing but spaces mean the same thing — no name — so store both
   * as null rather than letting "   " become someone's first name.
   */
  private static String normalize(String name) {
    return name == null || name.isBlank() ? null : name.trim();
  }
}
