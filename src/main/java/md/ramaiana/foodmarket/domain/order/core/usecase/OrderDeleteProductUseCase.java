package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderItemEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for deleting a product from an order.
 */
@UseCase
@RequiredArgsConstructor
public class OrderDeleteProductUseCase {

  private final OrderRepository orderRepository;

  /**
   * Execute the use case.
   */
  @Transactional(rollbackFor = Exception.class)
  public void execute(int orderId, int itemId) {
    if (itemId == 0) {
      throw new BadRequestException("Invalid order item ID");
    }

    OrderEntity order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
        .orElseThrow(() -> new NotFoundException(String.format("Order with ID [%s] not found", orderId)));

    OrderItemEntity itemToRemove = order.getItems().stream()
        .filter(item -> item.getId().equals(itemId))
        .findFirst()
        .orElseThrow(() -> new NotFoundException(String.format("Order item with ID [%s] not found", itemId)));

    order.removeItem(itemToRemove);
    orderRepository.save(order);
  }
}
