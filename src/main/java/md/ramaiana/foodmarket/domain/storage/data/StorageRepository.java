package md.ramaiana.foodmarket.domain.storage.data;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageRepository extends CrudRepository<StorageEntity, Integer> {

    AggregateReference<StorageEntity, Integer> getByErpCode(String erpCode);

}
