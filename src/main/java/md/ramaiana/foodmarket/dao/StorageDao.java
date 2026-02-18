package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.Storage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageDao extends CrudRepository<Storage, Long> {

}
