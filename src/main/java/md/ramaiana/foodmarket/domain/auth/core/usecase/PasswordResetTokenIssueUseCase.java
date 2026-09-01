package md.ramaiana.foodmarket.domain.auth.core.usecase;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.config.PasswordResetProperties;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.auth.data.PasswordResetTokenEntity;
import md.ramaiana.foodmarket.domain.auth.data.PasswordResetTokenRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.util.SecureTokenGenerator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Use case for issuing a password reset token — the database half of a reset. Retires any token the
 * user still holds, persists a fresh one, and builds the reset link. Sends nothing:
 * {@link PasswordResetMailUseCase} does that, outside the transaction this use case runs in.
 * <p>
 * Because every issue retires the previous tokens first, a user has at most one usable reset link at
 * any moment.
 */
@UseCase
@RequiredArgsConstructor
@EnableConfigurationProperties(PasswordResetProperties.class)
public class PasswordResetTokenIssueUseCase {

  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final SecureTokenGenerator secureTokenGenerator;
  private final PasswordResetProperties passwordResetProperties;

  /**
   * Everything the side-effect phase needs to email the reset link. Internal plumbing between a use
   * case's transactional phase and its side-effect phase — never bound from or to HTTP.
   * <p>
   * The raw reset token lives only inside {@code resetUrl}, in memory: the database holds nothing
   * but its hash.
   */
  public record TransactionalEffectResult(
      @NonNull AppUserEntity user,
      @NonNull String resetUrl,
      int expiresInHours
  ) {
  }

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(propagation = Propagation.MANDATORY)
  public TransactionalEffectResult execute(@NonNull AppUserEntity user) {
    Instant now = Instant.now();

    passwordResetTokenRepository.expireLiveTokensForUser(user.getId(), now);

    String rawToken = secureTokenGenerator.generate();
    Instant expiresAt = now.plus(passwordResetProperties.linkValidityHours(), ChronoUnit.HOURS);
    passwordResetTokenRepository.save(new PasswordResetTokenEntity(
        AggregateReference.to(user.getId()),
        secureTokenGenerator.hash(rawToken),
        expiresAt
    ));

    String resetUrl = UriComponentsBuilder.fromUriString(passwordResetProperties.resetPageUrl())
        .queryParam("resetToken", rawToken)
        .toUriString();

    return new TransactionalEffectResult(user, resetUrl, passwordResetProperties.linkValidityHours());
  }
}
