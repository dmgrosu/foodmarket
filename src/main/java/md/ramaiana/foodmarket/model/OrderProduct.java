package md.ramaiana.foodmarket.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@AllArgsConstructor
@Getter
@Builder
@Table("order_product")
public class OrderProduct {
    @Id
    @With
    private final Integer id;
    @Column("product_id")
    private final AggregateReference<@NonNull Product, @NonNull Integer> product;
    private final Float quantity;
    private final Float price;
    private final Float sum;
    private final Float weight;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OrderProduct that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, product);
    }

}
