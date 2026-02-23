package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for finding a user by email.
 */
@UseCase
@RequiredArgsConstructor
public class AppUserFindByEmailUseCase {

  private final AppUserRepository appUserRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public AppUserEntity execute(@NonNull String email) {
    return appUserRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundException(String.format("User with email '%s' not found", email)));
  }
}
