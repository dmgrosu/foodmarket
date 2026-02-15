package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.core.request.UpdateOrderRequest;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderItemEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import md.ramaiana.foodmarket.shared.util.SpecificationBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for updating an order.
 */
@UseCase
@RequiredArgsConstructor
public class OrderUpdateUseCase {

  private final OrderRepository orderRepository;

  /**
   * Execute the use case.
   */
  @Transactional(rollbackFor = Exception.class)
  public void execute(@NonNull UpdateOrderRequest request) {
    SpecificationBuilder<OrderEntity> specification = new SpecificationBuilder<>();
    specification.and(OrderRepository.idEquals(request.getOrderId()));
    specification.and(OrderRepository.notDeleted());

    OrderEntity order = orderRepository.findOne(specification.buildOrDefault())
        .orElseThrow(() -> new NotFoundException(String.format("Order with ID [%s] not found", request.getOrderId())));

    // Find the order item by product ID and update its quantity
    OrderItemEntity itemToUpdate = order.getItems().stream()
        .filter(item -> item.getProduct().getId().equals(request.getProductId()))
        .findFirst()
        .orElseThrow(() -> new NotFoundException(String.format("Product with ID [%s] not found in order", request.getProductId())));

    itemToUpdate.updateQuantity(request.getQuantity());
    order.updateTotalSum();
    orderRepository.save(order);
  }
}