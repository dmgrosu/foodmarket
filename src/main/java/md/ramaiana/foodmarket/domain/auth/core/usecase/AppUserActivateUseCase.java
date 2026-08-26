package md.ramaiana.foodmarket.domain.auth.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for an administrator activating a confirmed registration. Mixes a database write with an
 * email send, so it splits into the two phases a {@code RequestHandler} sequences —
 * {@code core/handler/AppUserActivateRequestHandler} — rather than one self-managing
 * {@code execute()}. There is nothing to validate before the read, so this has no
 * {@code preExecute} phase.
 */
@UseCase
@RequiredArgsConstructor
public class AppUserActivateUseCase {

  private final AppUserFindByIdUseCase appUserFindByIdUseCase;
  private final AppUserRepository appUserRepository;
  private final AccountActivatedMailUseCase accountActivatedMailUseCase;

  /**
   * The database half: flips a {@link UserState#CONFIRMED} user to {@link UserState#ACTIVE}.
   *
   * @throws BadRequestException if the user is not currently {@link UserState#CONFIRMED}.
   */
  @NonNull
  @Transactional(propagation = Propagation.MANDATORY)
  public AppUserEntity executeTransactionalEffect(@NonNull Integer userId) {
    AppUserEntity user = appUserFindByIdUseCase.execute(userId);
    if (user.getState() != UserState.CONFIRMED) {
      throw new BadRequestException(
          String.format("User '%s' is not awaiting activation (state: %s)", user.getEmail(), user.getState()));
    }
    return appUserRepository.save(user.withState(UserState.ACTIVE));
  }

  /**
   * The external-call half: emails the user that their account is active. Never lets a Mailjet
   * failure undo the activation — it is already committed by the time this runs.
   *
   * @return {@code true} if the email was sent, {@code false} if sending failed.
   */
  @Transactional(propagation = Propagation.NEVER)
  public boolean executeSideEffects(@NonNull AppUserEntity user) {
    return accountActivatedMailUseCase.execute(user);
  }
}
