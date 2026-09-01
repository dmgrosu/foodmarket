package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for deleting one of the caller's orders.
 */
@UseCase
@RequiredArgsConstructor
public class OrderDeleteUseCase {

  private final OrderRepository orderRepository;
  private final OrderLoader orderLoader;

  /**
   * Execute the use case.
   *
   * @throws BadRequestException if the order has already been placed. Once placed it is on its way
   *                             to the ERP, and deleting our copy would not recall it.
   */
  @Transactional(rollbackFor = Exception.class)
  public void execute(int orderId) {
    OrderEntity order = orderLoader.requireOwned(orderId);

    if (order.getState() != OrderState.NEW) {
      throw new BadRequestException(
          String.format("Order with ID [%s] has been placed and cannot be deleted", orderId));
    }

    order.markDeleted();
    orderRepository.save(order);
  }
}
