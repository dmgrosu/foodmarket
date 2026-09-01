package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for reading the caller's cart.
 * <p>
 * This is what lets a cart outlive a page reload: the client keeps no order id, it asks whose cart
 * it is holding on every load.
 */
@UseCase
@RequiredArgsConstructor
public class OrderCartFindUseCase {

  private final OrderLoader orderLoader;
  private final OrderResponseAssembler responseAssembler;

  /**
   * Execute the use case.
   *
   * @return the caller's cart, or an empty one if they have not started a cart yet.
   */
  @NonNull
  @Transactional(readOnly = true)
  public OrderResponse execute() {
    return orderLoader.findCart()
        .map(responseAssembler::assemble)
        .orElseGet(OrderResponse::emptyCart);
  }
}
