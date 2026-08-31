package md.ramaiana.foodmarket.domain.auth.data;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Password reset token Repository.
 */
public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetTokenEntity, Integer> {

  Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

  /**
   * Expire every still-live token for a user, so only the newest issued link works.
   */
  @Modifying
  @Query("update password_reset_token set expires_at = :now "
      + "where user_id = :userId and used_at is null and expires_at > :now")
  void expireLiveTokensForUser(@Param("userId") Integer userId, @Param("now") Instant now);

  /**
   * The most recently issued token for a user, used to enforce the request cooldown.
   */
  @Query("select * from password_reset_token where user_id = :userId order by created_at desc limit 1")
  Optional<PasswordResetTokenEntity> findLatestForUser(@Param("userId") Integer userId);

  /**
   * Every token ever issued to a user, most recent first.
   */
  @Query("select * from password_reset_token where user_id = :userId order by created_at desc")
  List<PasswordResetTokenEntity> findAllForUser(@Param("userId") Integer userId);
}
