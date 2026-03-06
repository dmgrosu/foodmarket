package md.ramaiana.foodmarket.domain.product.data;

import lombok.Getter;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("balances")
public class BalanceEntity {

    private final AggregateReference<StorageEntity, Integer> storage;
    private final AggregateReference<ProductEntity, Integer> product;
    private final Float quantity;

    @PersistenceCreator
    public BalanceEntity(AggregateReference<StorageEntity, Integer> storage,
                         AggregateReference<ProductEntity, Integer> product,
                         Float quantity) {
        this.storage = storage;
        this.product = product;
        this.quantity = quantity;
    }

}
