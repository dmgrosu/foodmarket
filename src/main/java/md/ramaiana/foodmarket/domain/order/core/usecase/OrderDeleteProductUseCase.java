package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for removing a product from the caller's cart.
 */
@UseCase
@RequiredArgsConstructor
public class OrderDeleteProductUseCase {

  private final OrderRepository orderRepository;
  private final OrderLoader orderLoader;
  private final OrderResponseAssembler responseAssembler;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(rollbackFor = Exception.class)
  public OrderResponse execute(int productId) {
    OrderEntity cart = orderLoader.requireCart();

    if (!cart.removeProduct(productId)) {
      throw new NotFoundException(String.format("Product with ID [%s] not found in cart", productId));
    }

    return responseAssembler.assemble(orderRepository.save(cart));
  }
}
