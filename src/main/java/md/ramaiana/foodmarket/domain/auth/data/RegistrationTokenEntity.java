package md.ramaiana.foodmarket.domain.auth.data;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Registration email confirmation token. Only a hash of the raw token is ever persisted.
 */
@Getter
@Table("registration_token")
public class RegistrationTokenEntity {

  @Id
  private final Integer id;
  @Column("user_id")
  @NonNull
  private final AggregateReference<AppUserEntity, Integer> user;
  @NonNull
  private final String tokenHash;
  @NonNull
  private final Instant expiresAt;
  private final Instant confirmedAt;
  @NonNull
  private final Instant createdAt;

  @PersistenceCreator
  public RegistrationTokenEntity(Integer id, @NonNull AggregateReference<AppUserEntity, Integer> user,
                                 @NonNull String tokenHash, @NonNull Instant expiresAt, Instant confirmedAt,
                                 @NonNull Instant createdAt) {
    this.id = id;
    this.user = user;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
    this.confirmedAt = confirmedAt;
    this.createdAt = createdAt;
  }

  public RegistrationTokenEntity(@NonNull AggregateReference<AppUserEntity, Integer> user,
                                 @NonNull String tokenHash, @NonNull Instant expiresAt) {
    this(null, user, tokenHash, expiresAt, null, Instant.now());
  }

  /**
   * Copy of this token, marked as confirmed at the given instant.
   */
  @NonNull
  public RegistrationTokenEntity withConfirmedAt(@NonNull Instant newConfirmedAt) {
    return new RegistrationTokenEntity(id, user, tokenHash, expiresAt, newConfirmedAt, createdAt);
  }

  public boolean isConfirmed() {
    return confirmedAt != null;
  }

  /**
   * A token is dead at its own expiry instant, not one tick after it. That matters because
   * RegistrationTokenRepository#expireLiveTokensForUser retires superseded tokens by setting
   * expires_at to the exact instant the replacement is issued.
   */
  public boolean isExpired(@NonNull Instant now) {
    return !now.isBefore(expiresAt);
  }
}
