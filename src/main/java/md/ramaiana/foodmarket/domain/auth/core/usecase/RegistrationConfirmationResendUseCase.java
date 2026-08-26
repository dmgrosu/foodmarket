package md.ramaiana.foodmarket.domain.auth.core.usecase;

import java.time.Instant;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.config.RegistrationProperties;
import md.ramaiana.foodmarket.domain.auth.core.request.RegistrationConfirmationResendRequest;
import md.ramaiana.foodmarket.domain.auth.core.usecase.RegistrationTokenIssueUseCase.TransactionalEffectResult;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenEntity;
import md.ramaiana.foodmarket.domain.auth.data.RegistrationTokenRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.enums.UserState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for resending a registration confirmation email — the recovery path when the original mail
 * failed to send or the link expired. Also the call an admin approval screen will reuse later.
 * <p>
 * Three-phase, same shape as {@link AuthRegisterUseCase}: {@link #executeTransactionalEffect} issues a
 * fresh token, {@link #executeSideEffects} emails it after that write has committed.
 */
@UseCase
@RequiredArgsConstructor
@EnableConfigurationProperties(RegistrationProperties.class)
public class RegistrationConfirmationResendUseCase {

  private final AppUserFindByEmailUseCase appUserFindByEmailUseCase;
  private final RegistrationTokenRepository registrationTokenRepository;
  private final RegistrationTokenIssueUseCase registrationTokenIssueUseCase;
  private final RegistrationConfirmationMailUseCase registrationConfirmationMailUseCase;
  private final RegistrationProperties registrationProperties;

  /**
   * Issue a fresh confirmation token, subject to the resend cooldown.
   */
  @NonNull
  @Transactional(propagation = Propagation.MANDATORY)
  public TransactionalEffectResult executeTransactionalEffect(@NonNull RegistrationConfirmationResendRequest request) {
    AppUserEntity user = appUserFindByEmailUseCase.execute(request.email());

    if (user.getState() != UserState.PENDING_CONFIRMATION) {
      throw new BadRequestException(
          String.format("User with email '%s' is not awaiting confirmation", request.email()));
    }

    registrationTokenRepository.findLatestForUser(user.getId())
        .map(RegistrationTokenEntity::getCreatedAt)
        .filter(lastIssuedAt -> lastIssuedAt.plusSeconds(registrationProperties.resendCooldownSeconds())
            .isAfter(Instant.now()))
        .ifPresent(lastIssuedAt -> {
          throw new BadRequestException("Confirmation email was already sent recently, please wait before retrying");
        });

    return registrationTokenIssueUseCase.execute(user);
  }

  /**
   * Email the confirmation link. Runs after the transactional effect has committed.
   *
   * @return {@code true} if the email was sent, {@code false} if sending failed.
   */
  public boolean executeSideEffects(@NonNull TransactionalEffectResult transactionalEffectResult) {
    return registrationConfirmationMailUseCase.execute(transactionalEffectResult);
  }
}
