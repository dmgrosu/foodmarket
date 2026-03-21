package md.ramaiana.foodmarket.domain.storage.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorageRepository extends CrudRepository<StorageEntity, Integer> {

    Optional<StorageEntity> findByErpCode(String erpCode);

}
