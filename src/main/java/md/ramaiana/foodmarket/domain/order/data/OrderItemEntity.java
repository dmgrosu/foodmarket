package md.ramaiana.foodmarket.domain.order.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Order Item Entity.
 * <p>
 * {@code id} is not a stable handle: Spring Data JDBC deletes and re-inserts {@code @MappedCollection}
 * children on every save of the owning {@link OrderEntity}, so it is regenerated whenever the order
 * changes. Callers address a line by its {@code productId}, which is the line's natural key —
 * {@link OrderEntity#addProduct} merges rather than appending, so a product appears at most once.
 */
@Getter
@Setter
@NoArgsConstructor
@Table("order_product")
public class OrderItemEntity {

  @Id
  private Integer id;
  @NonNull
  private Integer productId;
  @NonNull
  private Float quantity;
  @NonNull
  private Float price;
  @NonNull
  private Float sum;
  @NonNull
  private Float weight;

  /**
   * Constructor.
   */
  public OrderItemEntity(@NonNull Integer productId, @NonNull Float price,
                          float unitWeight, @NonNull Float quantity) {
    this.productId = productId;
    this.quantity = quantity;
    this.price = price;
    this.sum = price * quantity;
    this.weight = unitWeight * quantity;
  }

  public void updateQuantity(@NonNull Float newQuantity, float unitWeight) {
    this.quantity = newQuantity;
    this.sum = this.price * newQuantity;
    this.weight = unitWeight * newQuantity;
  }
}
