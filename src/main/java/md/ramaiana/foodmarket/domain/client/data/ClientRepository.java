package md.ramaiana.foodmarket.domain.client.data;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

/**
 * Client Repository.
 */
public interface ClientRepository extends CrudRepository<ClientEntity, Integer> {

  Optional<ClientEntity> findByIdnoAndDeletedAtIsNull(String idno);
}
