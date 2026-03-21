package md.ramaiana.foodmarket.domain.price.data;

import lombok.Getter;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.shared.enums.PriceType;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("prices")
public class PriceEntity {
    private final PriceType type;
    @Column("storage_id")
    private final AggregateReference<StorageEntity, Integer> storage;
    private final Float price;

    @PersistenceCreator
    public PriceEntity(PriceType type,
                       AggregateReference<StorageEntity, Integer> storage,
                       Float price) {
        this.type = type;
        this.storage = storage;
        this.price = price;
    }

    public PriceEntity(Float price) {
        this(null, null, price);
    }

}
