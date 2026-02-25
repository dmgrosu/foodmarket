package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for deleting an order.
 */
@UseCase
@RequiredArgsConstructor
public class OrderDeleteUseCase {

  private final OrderRepository orderRepository;

  /**
   * Execute the use case.
   */
  @Transactional(rollbackFor = Exception.class)
  public void execute(int orderId) {
    OrderEntity order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
        .orElseThrow(() -> new NotFoundException(String.format("Order with ID [%s] not found", orderId)));

    order.markDeleted();
    orderRepository.save(order);
  }
}
