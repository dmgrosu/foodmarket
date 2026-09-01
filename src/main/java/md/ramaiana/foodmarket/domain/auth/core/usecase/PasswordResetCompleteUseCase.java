package md.ramaiana.foodmarket.domain.auth.core.usecase;

import java.time.Instant;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.auth.core.request.PasswordResetCompleteRequest;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.AppUserRepository;
import md.ramaiana.foodmarket.domain.auth.data.PasswordResetTokenEntity;
import md.ramaiana.foodmarket.domain.auth.data.PasswordResetTokenRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.util.SecureTokenGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for completing a password reset with the token from the emailed link.
 * <p>
 * Self-transactional: no external call, so no handler. Every failure — unknown token, already-spent
 * token, expired token — collapses to a single undifferentiated {@link BadRequestException}, so the
 * endpoint never reveals whether a token ever existed.
 * <p>
 * Tokens already issued as JWTs stay valid: they are stateless and carry no version, so a session on
 * another device survives the reset until it expires on its own.
 */
@UseCase
@RequiredArgsConstructor
public class PasswordResetCompleteUseCase {

  private static final String INVALID_TOKEN_MESSAGE = "Password reset link is invalid or expired";

  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final AppUserRepository appUserRepository;
  private final AppUserFindByIdUseCase appUserFindByIdUseCase;
  private final SecureTokenGenerator secureTokenGenerator;
  private final PasswordEncoder passwordEncoder;

  /**
   * Execute the use case.
   */
  @Transactional(rollbackFor = Exception.class)
  public void execute(@NonNull PasswordResetCompleteRequest request) {
    PasswordResetTokenEntity token = passwordResetTokenRepository
        .findByTokenHash(secureTokenGenerator.hash(request.resetToken()))
        .orElseThrow(() -> new BadRequestException(INVALID_TOKEN_MESSAGE));

    Instant now = Instant.now();
    if (token.isUsed() || token.isExpired(now)) {
      throw new BadRequestException(INVALID_TOKEN_MESSAGE);
    }

    AppUserEntity user = appUserFindByIdUseCase.execute(token.getUser().getId());

    passwordResetTokenRepository.save(token.withUsedAt(now));
    // Retire any other live link, so an older email cannot be replayed after this reset.
    passwordResetTokenRepository.expireLiveTokensForUser(user.getId(), now);
    appUserRepository.save(user.withPassword(passwordEncoder.encode(request.newPassword())));
  }
}
