package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.Client;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Dmitri Grosu, 2/7/21
 */
@Repository
public interface ClientDao extends CrudRepository<Client, Integer> {

    Optional<Client> findByIdnoAndDeletedAtIsNull(String idno);

}
