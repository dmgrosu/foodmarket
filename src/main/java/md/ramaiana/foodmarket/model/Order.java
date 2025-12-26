package md.ramaiana.foodmarket.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
@Table("order")
public class Order {
    @Id
    @With
    private final Integer id;
    @Column("client_id")
    private final AggregateReference<@NonNull Client, @NonNull Integer> client;
    @Column("total_sum")
    private Float totalSum;
    @Column("created_at")
    @Builder.Default
    private final OffsetDateTime createdAt = OffsetDateTime.now();
    @Column("deleted_at")
    private final OffsetDateTime deletedAt;
    @Column("processed_at")
    private final OffsetDateTime processedAt;
    @Column("processing_result")
    private final String processingResult;
    @Column("status")
    private final OrderState state;
    @MappedCollection(idColumn = "id", keyColumn = "order_id")
    @Builder.Default
    private final List<OrderProduct> products = new ArrayList<>();

    public void addProduct(Product product, Float quantity) {
        products.add(OrderProduct.builder()
                .product(AggregateReference.to(product.getId()))
                .weight(product.getWeight() * quantity)
                .quantity(quantity)
                .sum(product.getPrice() * quantity)
                .build());
        updateTotalSum();
    }

    public Order updateQuantity(int productId, Float newQuantity) {
        List<OrderProduct> updatedProducts = products.stream()
                .map(op -> op.getProduct().getId() == productId ?
                        new OrderProduct(
                                op.getId(),
                                AggregateReference.to(productId),
                                newQuantity,
                                op.getPrice(),
                                op.getPrice() * newQuantity,
                                op.getWeight() * newQuantity
                        ) :
                        op)
                .toList();
        Float total = updatedProducts.stream()
                .map(OrderProduct::getSum)
                .reduce(Float::sum)
                .orElse(0f);
        return new Order(id, client, total, createdAt, deletedAt,
                processedAt, processingResult, state, updatedProducts);
    }

    public void removeProduct(int orderProductId) {
        products.removeIf(op -> op.getId() == orderProductId);
        updateTotalSum();
    }

    private void updateTotalSum() {
        this.totalSum = products.stream()
                .map(OrderProduct::getSum)
                .reduce(Float::sum)
                .orElse(0f);
    }

    public float getTotalWeightForProducts() {
        float result = 0f;
        if (products == null) return 0f;
        for (OrderProduct product : products) {
            result += product.getWeight();
        }
        return result;
    }

}
