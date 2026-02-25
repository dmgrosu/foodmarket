package md.ramaiana.foodmarket.domain.order.core.usecase;

import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.core.response.OrderItemResponse;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for finding an order by id.
 */
@UseCase
@RequiredArgsConstructor
public class OrderFindByIdUseCase {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(readOnly = true)
  public OrderResponse execute(int orderId) {
    if (orderId == 0) {
      throw new BadRequestException("Order ID cannot be zero");
    }

    OrderEntity order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
        .orElseThrow(() -> new NotFoundException(String.format("Order with ID [%s] not found", orderId)));

    List<OrderItemResponse> items = order.getItems().stream()
        .map(item -> new OrderItemResponse(
            item,
            productRepository.findNameById(item.getProductId())
        ))
        .collect(Collectors.toList());

    return new OrderResponse(order, items);
  }
}
