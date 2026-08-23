package md.ramaiana.foodmarket.domain.client.data;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface ClientRepository extends CrudRepository<ClientEntity, Integer>, ClientRepositoryCustom {

    Optional<ClientEntity> findByIdnoAndDeletedAtIsNull(String idno);

}
