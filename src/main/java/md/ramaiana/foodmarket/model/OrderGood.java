package md.ramaiana.foodmarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author Kirill Grosu (grosukirill009@gmail.com, 2/11/2021
 */

@AllArgsConstructor
@Builder
@Data
@Table("order_good")
public class OrderGood {
    @Id
    Integer id;
    @Column("order_id")
    Integer orderId;
    @Column("good_id")
    Integer goodId;
    Float quantity;
    Float sum;
    Float weight;
}
