package md.ramaiana.foodmarket.domain.order.core.usecase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.core.response.OrderItemResponse;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderItemEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import org.springframework.stereotype.Component;

/**
 * Puts product names on order lines.
 * <p>
 * Not a use case — it has no {@code execute} and answers no request of its own; it is the one piece
 * every order-returning use case shares, kept here rather than copied into six of them. The same
 * shape as {@code ProductGroupNaming} in the product domain.
 * <p>
 * The names are read in a single query per call. Resolving them line by line costs a query per item
 * per order, which across a page of orders is the difference between one query and hundreds.
 */
@Component
@RequiredArgsConstructor
public class OrderResponseAssembler {

  /** A product deleted after it was ordered still has to render on the historical order. */
  private static final String UNKNOWN_PRODUCT_NAME = "";

  private final ProductRepository productRepository;

  @NonNull
  public OrderResponse assemble(@NonNull OrderEntity order) {
    return assembleAll(List.of(order)).getFirst();
  }

  /**
   * Assemble a whole page of orders, reading every product name they mention at once.
   */
  @NonNull
  public List<OrderResponse> assembleAll(@NonNull Collection<OrderEntity> orders) {
    Set<Integer> productIds = orders.stream()
        .flatMap(order -> order.getItems().stream())
        .map(OrderItemEntity::getProductId)
        .collect(Collectors.toSet());

    Map<Integer, String> namesById = productRepository.findNamesByIds(productIds);

    return orders.stream()
        .map(order -> new OrderResponse(order, toItems(order, namesById)))
        .toList();
  }

  @NonNull
  private List<OrderItemResponse> toItems(@NonNull OrderEntity order,
                                          @NonNull Map<Integer, String> namesById) {
    return order.getItems().stream()
        .map(item -> new OrderItemResponse(
            item,
            namesById.getOrDefault(item.getProductId(), UNKNOWN_PRODUCT_NAME)))
        .sorted((left, right) -> left.getProductName().compareToIgnoreCase(right.getProductName()))
        .toList();
  }
}
