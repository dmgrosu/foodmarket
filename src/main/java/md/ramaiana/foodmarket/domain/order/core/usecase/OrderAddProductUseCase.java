package md.ramaiana.foodmarket.domain.order.core.usecase;

import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientRepository;
import md.ramaiana.foodmarket.domain.order.core.request.AddProductToOrderRequest;
import md.ramaiana.foodmarket.domain.order.core.response.OrderItemResponse;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderItemEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import md.ramaiana.foodmarket.shared.util.SpecificationBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for adding a product to an order.
 */
@UseCase
@RequiredArgsConstructor
public class OrderAddProductUseCase {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final ClientRepository clientRepository;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(rollbackFor = Exception.class)
  public OrderResponse execute(@NonNull AddProductToOrderRequest request) {
    // Validate client
    ClientEntity client = clientRepository.findById(request.getClientId())
        .orElseThrow(() -> new NotFoundException(String.format("Client with ID [%s] not found", request.getClientId())));

    // Find product using specification
    SpecificationBuilder<@NonNull ProductEntity> productSpec = new SpecificationBuilder<>();
    productSpec.and(ProductRepository.idEquals(request.getProductId()));
    productSpec.and(ProductRepository.notDeleted());

    ProductEntity product = productRepository.findOne(productSpec.buildOrDefault())
        .orElseThrow(() -> new NotFoundException(String.format("Product with ID [%s] not found", request.getProductId())));

    // Find or create order
    OrderEntity order = findOrCreateOrder(request.getOrderId(), client);

    // Add product to order
    OrderItemEntity item = new OrderItemEntity(product, request.getQuantity());
    order.addItem(item);
    OrderEntity savedOrder = orderRepository.save(order);

    // Build response
    List<OrderItemResponse> items = savedOrder.getItems().stream()
        .map(orderItem -> new OrderItemResponse(
            orderItem,
            productRepository.findNameById(orderItem.getProduct().getId())
        ))
        .collect(Collectors.toList());

    return new OrderResponse(savedOrder, items);
  }

  @NonNull
  private OrderEntity findOrCreateOrder(int orderId, ClientEntity client) {
    OrderEntity order = orderId != 0 ?
        findOrder(orderId) :
        new OrderEntity(client);

    if (order.getState() == OrderState.PROCESSED) {
      throw new BadRequestException(String.format("Order with ID [%s] has been already processed", orderId));
    }
    if (order.getState() == OrderState.PLACED) {
      throw new BadRequestException(String.format("Order with ID [%s] has been already placed", orderId));
    }
    return order;
  }

  private OrderEntity findOrder(int orderId) {
    SpecificationBuilder<OrderEntity> specification = new SpecificationBuilder<>();
    specification.and(OrderRepository.idEquals(orderId));
    specification.and(OrderRepository.notDeleted());

    return orderRepository.findOne(specification.buildOrDefault())
        .orElseThrow(() -> new NotFoundException(String.format("Order with ID [%s] not found", orderId)));
  }
}