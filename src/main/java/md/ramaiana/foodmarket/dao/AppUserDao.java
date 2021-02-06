package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.AppUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Dmitri Grosu, 2/6/21
 */
@Repository
public interface AppUserDao extends CrudRepository<AppUser, Integer> {

    AppUser findByEmail(String email);

    Boolean existsByEmail(String email);

}
