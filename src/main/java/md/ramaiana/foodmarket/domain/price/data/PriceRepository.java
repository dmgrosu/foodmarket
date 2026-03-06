package md.ramaiana.foodmarket.domain.price.data;

import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.shared.enums.PriceType;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PriceRepository extends CrudRepository<PriceEntity, Integer> {
    Optional<PriceEntity> findByStorageAndProductAndType(AggregateReference<StorageEntity, Integer> storage,
                                                         AggregateReference<ProductEntity, Integer> product,
                                                         PriceType type);
}
