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

  @NonNull
  private Integer id;

  @NonNull
  private String productName;

  private float quantity;

  private float price;

  private float sum;

  private float weight;

  public OrderItemResponse(@NonNull OrderItemEntity orderItem, @NonNull String productName) {
    this.id = orderItem.getId();
    this.productName = productName;
    this.quantity = orderItem.getQuantity();
    this.price = orderItem.getPrice();
    this.sum = orderItem.getSum();
    this.weight = orderItem.getWeight();
  }
}
