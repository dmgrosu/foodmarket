package md.ramaiana.foodmarket.domain.order.presentation.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.order.core.request.AddProductToOrderRequest;
import md.ramaiana.foodmarket.domain.order.core.request.OrderListRequest;
import md.ramaiana.foodmarket.domain.order.core.request.UpdateOrderProductRequest;
import md.ramaiana.foodmarket.domain.order.core.response.OrderListResponse;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderAddProductUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderCartClearUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderCartFindUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderDeleteProductUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderDeleteUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderFindByIdUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderPlaceUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderSearchByPeriodUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderUpdateProductUseCase;
import md.ramaiana.foodmarket.domain.order.presentation.voter.OrderAccessVoter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Order controller.
 * <p>
 * The cart endpoints take no order id: a client has at most one open cart and the server resolves it
 * from the authenticated user. The history endpoints take one, and it is checked against the
 * caller's client before anything is read.
 */
@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

  // Access voters
  private final OrderAccessVoter accessVoter;

  // Use cases
  private final OrderCartFindUseCase orderCartFindUseCase;
  private final OrderAddProductUseCase orderAddProductUseCase;
  private final OrderUpdateProductUseCase orderUpdateProductUseCase;
  private final OrderDeleteProductUseCase orderDeleteProductUseCase;
  private final OrderCartClearUseCase orderCartClearUseCase;
  private final OrderPlaceUseCase orderPlaceUseCase;
  private final OrderFindByIdUseCase orderFindByIdUseCase;
  private final OrderSearchByPeriodUseCase orderSearchByPeriodUseCase;
  private final OrderDeleteUseCase orderDeleteUseCase;

  /**
   * Get the current cart.
   */
  @GetMapping("/getCart")
  public OrderResponse getCart() {
    accessVoter.assertCanGetCart();
    return orderCartFindUseCase.execute();
  }

  /**
   * Add product to cart.
   */
  @PostMapping("/addProduct")
  public OrderResponse addProduct(@Valid @RequestBody @NonNull AddProductToOrderRequest request) {
    accessVoter.assertCanAddProduct();
    return orderAddProductUseCase.execute(request);
  }

  /**
   * Set the quantity of a product in the cart.
   */
  @PutMapping("/updateProduct")
  public OrderResponse updateProduct(@Valid @RequestBody @NonNull UpdateOrderProductRequest request) {
    accessVoter.assertCanUpdateProduct();
    return orderUpdateProductUseCase.execute(request);
  }

  /**
   * Delete product from cart.
   */
  @DeleteMapping("/deleteProduct/{productId}")
  public OrderResponse deleteProduct(@PathVariable int productId) {
    accessVoter.assertCanDeleteProduct();
    return orderDeleteProductUseCase.execute(productId);
  }

  /**
   * Empty the cart.
   */
  @DeleteMapping("/clearCart")
  public void clearCart() {
    accessVoter.assertCanClearCart();
    orderCartClearUseCase.execute();
  }

  /**
   * Place the cart as an order.
   */
  @PutMapping("/placeOrder")
  public OrderResponse placeOrder() {
    accessVoter.assertCanPlaceOrder();
    return orderPlaceUseCase.execute();
  }

  /**
   * Get order by id.
   */
  @GetMapping("/getById/{orderId}")
  public OrderResponse getById(@PathVariable int orderId) {
    accessVoter.assertCanGetById();
    return orderFindByIdUseCase.execute(orderId);
  }

  /**
   * Get orders by period.
   */
  @PostMapping("/getOrdersByPeriod")
  public OrderListResponse getOrdersByPeriod(@Valid @RequestBody @NonNull OrderListRequest request) {
    accessVoter.assertCanGetOrdersByPeriod();
    return orderSearchByPeriodUseCase.execute(request);
  }

  /**
   * Delete order.
   */
  @DeleteMapping("/deleteById/{orderId}")
  public void deleteById(@PathVariable int orderId) {
    accessVoter.assertCanDelete();
    orderDeleteUseCase.execute(orderId);
  }
}
