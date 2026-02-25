package md.ramaiana.foodmarket.domain.auth.data;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

/**
 * AppUser Repository.
 */
public interface AppUserRepository extends CrudRepository<AppUserEntity, Integer> {

  Optional<AppUserEntity> findByEmail(String email);
}
