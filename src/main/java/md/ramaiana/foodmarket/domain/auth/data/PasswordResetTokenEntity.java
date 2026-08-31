package md.ramaiana.foodmarket.domain.auth.data;

import java.time.Instant;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Password reset token. Only a hash of the raw token is ever persisted, so a database leak does not
 * hand out working reset links.
 */
@Getter
@Table("password_reset_token")
public class PasswordResetTokenEntity {

  @Id
  private final Integer id;
  @Column("user_id")
  @NonNull
  private final AggregateReference<AppUserEntity, Integer> user;
  @NonNull
  private final String tokenHash;
  @NonNull
  private final Instant expiresAt;
  private final Instant usedAt;
  @NonNull
  private final Instant createdAt;

  @PersistenceCreator
  public PasswordResetTokenEntity(Integer id, @NonNull AggregateReference<AppUserEntity, Integer> user,
                                  @NonNull String tokenHash, @NonNull Instant expiresAt, Instant usedAt,
                                  @NonNull Instant createdAt) {
    this.id = id;
    this.user = user;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.usedAt = usedAt;
    this.createdAt = createdAt;
  }

  public PasswordResetTokenEntity(@NonNull AggregateReference<AppUserEntity, Integer> user,
                                  @NonNull String tokenHash, @NonNull Instant expiresAt) {
    this(null, user, tokenHash, expiresAt, null, Instant.now());
  }

  /**
   * Copy of this token, spent at the given instant. A reset link works exactly once.
   */
  @NonNull
  public PasswordResetTokenEntity withUsedAt(@NonNull Instant newUsedAt) {
    return new PasswordResetTokenEntity(id, user, tokenHash, expiresAt, newUsedAt, createdAt);
  }

  public boolean isUsed() {
    return usedAt != null;
  }

  /**
   * A token is dead at its own expiry instant, not one tick after it. That matters because
   * PasswordResetTokenRepository#expireLiveTokensForUser retires superseded tokens by setting
   * expires_at to the exact instant the replacement is issued.
   */
  public boolean isExpired(@NonNull Instant now) {
    return !now.isBefore(expiresAt);
  }
}
