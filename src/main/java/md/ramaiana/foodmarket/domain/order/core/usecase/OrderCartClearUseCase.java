package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for emptying the caller's cart.
 * <p>
 * The cart is dropped outright rather than left standing and empty, so the next add is free to open
 * one against a different storage or price tier.
 */
@UseCase
@RequiredArgsConstructor
public class OrderCartClearUseCase {

  private final OrderRepository orderRepository;
  private final OrderLoader orderLoader;

  /**
   * Execute the use case. Emptying a cart that was never started is not an error.
   */
  @Transactional(rollbackFor = Exception.class)
  public void execute() {
    orderLoader.findCart().ifPresent(this::discard);
  }

  private void discard(OrderEntity cart) {
    cart.clearItems();
    cart.markDeleted();
    orderRepository.save(cart);
  }
}
