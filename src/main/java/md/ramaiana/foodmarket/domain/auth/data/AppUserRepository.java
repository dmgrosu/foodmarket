package md.ramaiana.foodmarket.domain.auth.data;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

/**
 * AppUser Repository.
 */
public interface AppUserRepository extends CrudRepository<AppUserEntity, Integer>, AppUserRepositoryCustom {

  Optional<AppUserEntity> findByEmail(String email);
}
