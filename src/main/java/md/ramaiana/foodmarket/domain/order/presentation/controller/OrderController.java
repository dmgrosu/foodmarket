package md.ramaiana.foodmarket.domain.order.presentation.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.domain.order.core.request.AddProductToOrderRequest;
import md.ramaiana.foodmarket.domain.order.core.request.OrderListRequest;
import md.ramaiana.foodmarket.domain.order.core.request.UpdateOrderRequest;
import md.ramaiana.foodmarket.domain.order.core.response.OrderListResponse;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderAddProductUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderDeleteProductUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderDeleteUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderFindByIdUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderPlaceUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderSearchByPeriodUseCase;
import md.ramaiana.foodmarket.domain.order.core.usecase.OrderUpdateUseCase;
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
 */
@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

  // Access voters
  private final OrderAccessVoter accessVoter;

  // Use cases
  private final OrderAddProductUseCase orderAddProductUseCase;
  private final OrderFindByIdUseCase orderFindByIdUseCase;
  private final OrderDeleteUseCase orderDeleteUseCase;
  private final OrderDeleteProductUseCase orderDeleteProductUseCase;
  private final OrderSearchByPeriodUseCase orderSearchByPeriodUseCase;
  private final OrderUpdateUseCase orderUpdateUseCase;
  private final OrderPlaceUseCase orderPlaceUseCase;

  /**
   * Add product to order.
   */
  @PostMapping("/addProduct")
  public OrderResponse addProduct(@Valid @RequestBody @NonNull AddProductToOrderRequest request) {
    accessVoter.assertCanAddProduct();
    return orderAddProductUseCase.execute(request);
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
   * Delete order.
   */
  @DeleteMapping("/deleteById/{orderId}")
  public void deleteById(@PathVariable int orderId) {
    accessVoter.assertCanDelete();
    orderDeleteUseCase.execute(orderId);
  }

  /**
   * Delete product from order.
   */
  @DeleteMapping("/deleteProduct/{orderId}/{itemId}")
  public void deleteProduct(@PathVariable int orderId, @PathVariable int itemId) {
    accessVoter.assertCanDeleteProduct();
    orderDeleteProductUseCase.execute(orderId, itemId);
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
   * Update order.
   */
  @PutMapping("/update")
  public void update(@Valid @RequestBody @NonNull UpdateOrderRequest request) {
    accessVoter.assertCanUpdate();
    orderUpdateUseCase.execute(request);
  }

  /**
   * Place order.
   */
  @PutMapping("/placeOrder/{orderId}")
  public void placeOrder(@PathVariable int orderId) {
    accessVoter.assertCanPlaceOrder();
    orderPlaceUseCase.execute(orderId);
  }
}