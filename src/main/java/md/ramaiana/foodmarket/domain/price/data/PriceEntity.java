package md.ramaiana.foodmarket.domain.price.data;

import lombok.Getter;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.storage.data.StorageEntity;
import md.ramaiana.foodmarket.shared.enums.PriceType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("prices")
public class PriceEntity {
    @Id
    private final Integer id;
    private final PriceType type;
    @Column("product_id")
    private final AggregateReference<ProductEntity, Integer> product;
    @Column("storage_id")
    private final AggregateReference<StorageEntity, Integer> storage;
    private final Float price;

    @PersistenceCreator
    public PriceEntity(Integer id, PriceType type,
                       AggregateReference<StorageEntity, Integer> storage,
                       AggregateReference<ProductEntity, Integer> product,
                       Float price) {
        this.id = id;
        this.type = type;
        this.product = product;
        this.storage = storage;
        this.price = price;
    }

    public PriceEntity(Float price) {
        this(null, null, null, null, price);
    }

    public PriceEntity(PriceType type, Float price) {
        this(null, type, null, null, price);
    }

    public PriceEntity withPrice(Float price) {
        return new PriceEntity(this.id, this.type, this.storage, this.product, price);
    }

}
