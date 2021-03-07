package md.ramaiana.foodmarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author Kirill Grosu (grosukirill009@gmail.com), 2/11/2021
 */
@AllArgsConstructor
@Data
@Builder
@Table("order")
public class Order {
    @Id
    Integer id;
    @Column("client_id")
    Integer clientId;
    @Column("total_sum")
    Float totalSum;
    @Column("created_at")
    OffsetDateTime createdAt;
    @Column("deleted_at")
    OffsetDateTime deletedAt;
    @Column("processed_at")
    OffsetDateTime processedAt;
    @Column("processing_result")
    String processingResult;
    @MappedCollection(idColumn = "good_id")
    List<OrderGood> goods;

    public float getTotalSumForGoods() {
        float result = 0f;
        for (OrderGood good : goods) {
            result += good.sum;
        }
        return result;
    }

    public float getTotalWeightForGoods() {
        float result = 0f;
        for (OrderGood good : goods) {
            result += good.weight;
        }
        return result;
    }
}
