package md.ramaiana.foodmarket.domain.auth.data;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Registration confirmation token Repository.
 */
public interface RegistrationTokenRepository extends CrudRepository<RegistrationTokenEntity, Integer> {

  Optional<RegistrationTokenEntity> findByTokenHash(String tokenHash);

  /**
   * Expire every still-live token for a user, so only the newest issued link works.
   */
  @Modifying
  @Query("update registration_token set expires_at = :now "
      + "where user_id = :userId and confirmed_at is null and expires_at > :now")
  void expireLiveTokensForUser(@Param("userId") Integer userId, @Param("now") Instant now);

  /**
   * The most recently issued token for a user, used to enforce the resend cooldown.
   */
  @Query("select * from registration_token where user_id = :userId order by created_at desc limit 1")
  Optional<RegistrationTokenEntity> findLatestForUser(@Param("userId") Integer userId);

  /**
   * Every token ever issued to a user, most recent first.
   */
  @Query("select * from registration_token where user_id = :userId order by created_at desc")
  List<RegistrationTokenEntity> findAllForUser(@Param("userId") Integer userId);
}
