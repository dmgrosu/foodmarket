package md.ramaiana.foodmarket.domain.auth.core.usecase;

import java.time.Instant;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.config.PasswordResetProperties;
import md.ramaiana.foodmarket.domain.auth.core.request.PasswordResetInitiateRequest;
import md.ramaiana.foodmarket.domain.auth.core.usecase.PasswordResetTokenIssueUseCase.TransactionalEffectResult;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.auth.data.PasswordResetTokenEntity;
import md.ramaiana.foodmarket.domain.auth.data.PasswordResetTokenRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.enums.UserState;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for requesting a password reset link.
 * <p>
 * Three-phase, same shape as {@link RegistrationConfirmationResendUseCase}, with one difference that
 * drives the whole design: <b>this endpoint must never reveal whether an address is registered.</b>
 * An unauthenticated caller who could tell "unknown email" from "link sent" would have an
 * account-existence oracle, so every rejection is silent — the transactional phase returns an empty
 * Optional, the handler skips the email, and the response is identical either way.
 * <p>
 * That is why the lookup goes through {@link AppUserRepository} directly rather than
 * {@link AppUserFindByEmailUseCase}, which throws {@code NotFoundException} — the very leak being
 * avoided. It also diverges deliberately from {@code /auth/resendConfirmation}, which does propagate
 * that exception; changing resend is a separate decision with its own asserted test contract.
 */
@UseCase
@RequiredArgsConstructor
@EnableConfigurationProperties(PasswordResetProperties.class)
public class PasswordResetInitiateUseCase {

  private final AppUserRepository appUserRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordResetTokenIssueUseCase passwordResetTokenIssueUseCase;
  private final PasswordResetMailUseCase passwordResetMailUseCase;
  private final PasswordResetProperties passwordResetProperties;

  /**
   * Issue a reset token, unless there is no reason to.
   *
   * @return the issued token, or empty when nothing should be sent — no such user, a user who cannot
   *     sign in yet anyway, or a request inside the cooldown window.
   */
  @NonNull
  @Transactional(propagation = Propagation.MANDATORY)
  public Optional<TransactionalEffectResult> executeTransactionalEffect(
      @NonNull PasswordResetInitiateRequest request) {
    Optional<AppUserEntity> found = appUserRepository.findByEmail(request.email());
    if (found.isEmpty()) {
      return Optional.empty();
    }

    AppUserEntity user = found.get();
    if (user.getState() != UserState.ACTIVE) {
      // A user who has not confirmed their email needs /auth/resendConfirmation, not a reset; a
      // suspended one should not be handed a way back in.
      return Optional.empty();
    }

    boolean insideCooldown = passwordResetTokenRepository.findLatestForUser(user.getId())
        .map(PasswordResetTokenEntity::getCreatedAt)
        .filter(lastIssuedAt -> lastIssuedAt.plusSeconds(passwordResetProperties.requestCooldownSeconds())
            .isAfter(Instant.now()))
        .isPresent();
    if (insideCooldown) {
      return Optional.empty();
    }

    return Optional.of(passwordResetTokenIssueUseCase.execute(user));
  }

  /**
   * Email the reset link. Runs after the transactional effect has committed.
   *
   * @return {@code true} if the email was sent, {@code false} if sending failed.
   */
  public boolean executeSideEffects(@NonNull TransactionalEffectResult transactionalEffectResult) {
    return passwordResetMailUseCase.execute(transactionalEffectResult);
  }
}
