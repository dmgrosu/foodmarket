package md.ramaiana.foodmarket.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("prices")
@Getter
public class Price {
    @Id
    private final Integer id;
    private final PriceType type;
    @Column("product_id")
    private final AggregateReference<Product, Integer> product;
    @Column("storage_id")
    private final AggregateReference<Storage, Integer> storage;
    private final Float price;

    @PersistenceCreator
    public Price(Integer id, PriceType type, AggregateReference<Product, Integer> product,
                 AggregateReference<Storage, Integer> storage, Float price) {
        this.id = id;
        this.type = type;
        this.product = product;
        this.storage = storage;
        this.price = price;
    }

    public Price(Float price) {
        this(null, null, null, null, price);
    }

    public Price(PriceType type, Float price) {
        this(null, type, null, null, price);
    }

}
