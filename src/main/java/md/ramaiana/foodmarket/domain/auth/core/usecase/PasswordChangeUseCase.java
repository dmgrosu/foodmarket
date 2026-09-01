package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.request.PasswordChangeRequest;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for changing the signed-in user's own password.
 * <p>
 * Note that tokens already issued stay valid: the JWTs here are stateless and carry no version, so
 * a session on another device survives the change until it expires on its own.
 */
@UseCase
@RequiredArgsConstructor
public class PasswordChangeUseCase {

  private final AppUserRepository appUserRepository;
  private final AppUserFindByIdUseCase appUserFindByIdUseCase;
  private final PasswordEncoder passwordEncoder;

  /**
   * Execute the use case.
   */
  @Transactional(rollbackFor = Exception.class)
  public void execute(@NonNull AppUserEntity currentUser, @NonNull PasswordChangeRequest request) {
    // Reload inside the transaction: the principal was read by the JWT filter, before this write began.
    AppUserEntity user = appUserFindByIdUseCase.execute(currentUser.getId());

    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswd())) {
      throw new BadRequestException("Current password is incorrect");
    }
    if (passwordEncoder.matches(request.newPassword(), user.getPasswd())) {
      throw new BadRequestException("New password must differ from the current one");
    }

    appUserRepository.save(user.withPassword(passwordEncoder.encode(request.newPassword())));
  }
}
