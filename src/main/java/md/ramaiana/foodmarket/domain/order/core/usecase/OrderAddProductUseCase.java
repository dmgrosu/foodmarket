package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.core.request.AddProductToOrderRequest;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.enums.PriceType;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for adding a product to the caller's cart.
 * <p>
 * Opens the cart if there is not one already. Adding a product the cart already holds increases that
 * line rather than appending a second one — {@link OrderUpdateProductUseCase} is what sets a
 * quantity outright.
 */
@UseCase
@RequiredArgsConstructor
public class OrderAddProductUseCase {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final OrderLoader orderLoader;
  private final OrderResponseAssembler responseAssembler;

  /**
   * Execute the use case.
   */
  @NonNull
  @Transactional(rollbackFor = Exception.class)
  public OrderResponse execute(@NonNull AddProductToOrderRequest request) {
    ProductEntity product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
        .orElseThrow(() -> new NotFoundException(
            String.format("Product with ID [%s] not found", request.getProductId())));

    OrderEntity cart = findOrOpenCart(request.getStorageId(), request.getPriceType());

    float price = product.findPrice(request.getStorageId(), request.getPriceType())
        .orElseThrow(() -> new BadRequestException(String.format(
            "Product with ID [%s] has no %s price in storage [%s]",
            request.getProductId(), request.getPriceType(), request.getStorageId())));

    float unitWeight = product.getWeight() != null ? product.getWeight() : 0f;
    cart.addProduct(product.getId(), price, unitWeight, request.getQuantity());

    return responseAssembler.assemble(orderRepository.save(cart));
  }

  /**
   * The caller's cart, opened against this storage and tier if they have none.
   *
   * @throws BadRequestException if they already have one against a different storage or tier. A
   *                             cart's lines are only comparable when they are all priced the same
   *                             way, and an order ships from one warehouse.
   */
  @NonNull
  private OrderEntity findOrOpenCart(@NonNull Integer storageId, @NonNull PriceType priceType) {
    return orderLoader.findCart()
        .map(cart -> assertMatches(cart, storageId, priceType))
        .orElseGet(() -> new OrderEntity(orderLoader.getCurrentClientId(), storageId, priceType));
  }

  @NonNull
  private OrderEntity assertMatches(@NonNull OrderEntity cart, @NonNull Integer storageId,
                                    @NonNull PriceType priceType) {
    if (!storageId.equals(cart.getStorageId())) {
      throw new BadRequestException(String.format(
          "Cart is for storage [%s]; empty it before ordering from storage [%s]",
          cart.getStorageId(), storageId));
    }
    if (priceType != cart.getPriceType()) {
      throw new BadRequestException(String.format(
          "Cart is priced [%s]; empty it before ordering at [%s]", cart.getPriceType(), priceType));
    }
    return cart;
  }
}
