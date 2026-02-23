package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.core.request.UpdateOrderRequest;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderItemEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for updating an order.
 */
@UseCase
@RequiredArgsConstructor
public class OrderUpdateUseCase {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;

  /**
   * Execute the use case.
   */
  @Transactional(rollbackFor = Exception.class)
  public void execute(@NonNull UpdateOrderRequest request) {
    OrderEntity order = orderRepository.findByIdAndDeletedAtIsNull(request.getOrderId())
        .orElseThrow(() -> new NotFoundException(String.format("Order with ID [%s] not found", request.getOrderId())));

    // Find the order item by product ID and update its quantity
    OrderItemEntity itemToUpdate = order.getItems().stream()
        .filter(item -> item.getProductId().equals(request.getProductId()))
        .findFirst()
        .orElseThrow(() -> new NotFoundException(String.format("Product with ID [%s] not found in order", request.getProductId())));

    // Load product to get unit weight
    ProductEntity product = productRepository.findByIdAndDeletedAtIsNull(itemToUpdate.getProductId())
        .orElseThrow(() -> new NotFoundException(String.format("Product with ID [%s] not found", itemToUpdate.getProductId())));
    float unitWeight = product.getWeight() != null ? product.getWeight() : 0f;

    itemToUpdate.updateQuantity(request.getQuantity(), unitWeight);
    order.updateTotalSum();
    orderRepository.save(order);
  }
}
