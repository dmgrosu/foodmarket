package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for placing an order.
 */
@UseCase
@RequiredArgsConstructor
public class OrderPlaceUseCase {

  private final OrderRepository orderRepository;

  /**
   * Execute the use case.
   */
  @Transactional(rollbackFor = Exception.class)
  public void execute(int orderId) {
    if (orderId == 0) {
      throw new BadRequestException("Order ID cannot be zero");
    }

    OrderEntity order = orderRepository.findByIdAndDeletedAtIsNull(orderId)
        .orElseThrow(() -> new NotFoundException(String.format("Order with id [%s] not found", orderId)));

    order.setState(OrderState.PLACED);
    orderRepository.save(order);
  }
}
