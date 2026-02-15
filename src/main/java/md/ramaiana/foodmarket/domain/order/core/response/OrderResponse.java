package md.ramaiana.foodmarket.domain.order.core.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.shared.enums.OrderState;

/**
 * Order response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

  private Integer id;

  private float totalSum;

  private Integer clientId;

  @NonNull
  private OrderState state;

  private long createdAt;

  private float totalWeight;

  @NonNull
  private List<OrderItemResponse> items;

  public OrderResponse(@NonNull OrderEntity order, @NonNull List<OrderItemResponse> items) {
    this.id = order.getId();
    this.totalSum = order.getTotalSum();
    this.clientId = order.getClient() != null ? order.getClient().getId() : null;
    this.state = order.getState();
    this.createdAt = order.getCreatedAt().toEpochMilli();
    this.totalWeight = order.getTotalWeightForProducts();
    this.items = items;
  }
}
