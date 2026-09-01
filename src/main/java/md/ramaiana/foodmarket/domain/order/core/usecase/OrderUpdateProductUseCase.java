package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.core.request.UpdateOrderProductRequest;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for setting the quantity of a line in the caller's cart.
 */
@UseCase
@RequiredArgsConstructor
public class OrderUpdateProductUseCase {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final OrderLoader orderLoader;
  private final OrderResponseAssembler responseAssembler;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(rollbackFor = Exception.class)
  public OrderResponse execute(@NonNull UpdateOrderProductRequest request) {
    OrderEntity cart = orderLoader.requireCart();

    // The line's weight is quantity times unit weight, so changing one needs the other.
    ProductEntity product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
        .orElseThrow(() -> new NotFoundException(
            String.format("Product with ID [%s] not found", request.getProductId())));
    float unitWeight = product.getWeight() != null ? product.getWeight() : 0f;

    if (!cart.setProductQuantity(request.getProductId(), request.getQuantity(), unitWeight)) {
      throw new NotFoundException(
          String.format("Product with ID [%s] not found in cart", request.getProductId()));
    }

    return responseAssembler.assemble(orderRepository.save(cart));
  }
}
