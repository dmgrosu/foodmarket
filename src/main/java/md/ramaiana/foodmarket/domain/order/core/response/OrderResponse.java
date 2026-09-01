package md.ramaiana.foodmarket.domain.order.core.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import md.ramaiana.foodmarket.shared.enums.PriceType;

/**
 * Order response. Also the cart: an empty cart is {@link #emptyCart()}, an order with no id.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

  private Integer id;

  private float totalSum;

  private Integer clientId;

  /**
   * The storage the order is priced against. Fixed at creation, so the caller can tell which
   * storage a non-empty cart is locked to.
   */
  private Integer storageId;

  private PriceType priceType;

  @NonNull
  private OrderState state;

  private long createdAt;

  private float totalWeight;

  @NonNull
  private List<OrderItemResponse> items;

  public OrderResponse(@NonNull OrderEntity order, @NonNull List<OrderItemResponse> items) {
    this.id = order.getId();
    this.totalSum = order.getTotalSum();
    this.clientId = order.getClientId();
    this.storageId = order.getStorageId();
    this.priceType = order.getPriceType();
    this.state = order.getState();
    this.createdAt = order.getCreatedAt().toEpochMilli();
    this.totalWeight = order.getTotalWeightForProducts();
    this.items = items;
  }

  /**
   * The response for a client who has no cart yet. A null id tells the caller there is nothing on
   * the server to update or place, without making them special-case a 404.
   */
  @NonNull
  public static OrderResponse emptyCart() {
    OrderResponse response = new OrderResponse();
    response.state = OrderState.NEW;
    response.items = List.of();
    return response;
  }
}
