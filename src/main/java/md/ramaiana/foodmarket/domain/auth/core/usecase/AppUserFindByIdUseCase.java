package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for finding a user by ID.
 */
@UseCase
@RequiredArgsConstructor
public class AppUserFindByIdUseCase {

  private final AppUserRepository appUserRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public AppUserEntity execute(@NonNull Integer id) {
    return appUserRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(String.format("User with id '%s' not found", id)));
  }
}
