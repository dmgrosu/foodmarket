package md.ramaiana.foodmarket.domain.order.core.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.order.data.OrderItemEntity;

/**
 * Order item response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

  /**
   * The line's own id. Not a handle to address it by — it is regenerated whenever the order is
   * saved; see {@link OrderItemEntity}. Callers use {@link #productId}.
   */
  private Integer id;

  @NonNull
  private Integer productId;

  @NonNull
  private String productName;

  private float quantity;

  private float price;

  private float sum;

  private float weight;

  public OrderItemResponse(@NonNull OrderItemEntity orderItem, @NonNull String productName) {
    this.id = orderItem.getId();
    this.productId = orderItem.getProductId();
    this.productName = productName;
    this.quantity = orderItem.getQuantity();
    this.price = orderItem.getPrice();
    this.sum = orderItem.getSum();
    this.weight = orderItem.getWeight();
  }
}
