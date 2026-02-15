package md.ramaiana.foodmarket.domain.order.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Order", description = "Order management endpoints")
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
  @Operation(
      operationId = "addProductToOrder",
      summary = "Add product to order",
      description = "Add a product to an existing order or create a new order"
  )
  public OrderResponse addProduct(@Valid @RequestBody @NonNull AddProductToOrderRequest request) {
    accessVoter.assertCanAddProduct();
    return orderAddProductUseCase.execute(request);
  }

  /**
   * Get order by id.
   */
  @GetMapping("/getById/{orderId}")
  @Operation(
      operationId = "getOrderById",
      summary = "Get order by ID",
      description = "Retrieve an order with all its items by order ID"
  )
  public OrderResponse getById(@PathVariable int orderId) {
    accessVoter.assertCanGetById();
    return orderFindByIdUseCase.execute(orderId);
  }

  /**
   * Delete order.
   */
  @DeleteMapping("/deleteById/{orderId}")
  @Operation(
      operationId = "deleteOrder",
      summary = "Delete order",
      description = "Soft delete an order by ID"
  )
  public void deleteById(@PathVariable int orderId) {
    accessVoter.assertCanDelete();
    orderDeleteUseCase.execute(orderId);
  }

  /**
   * Delete product from order.
   */
  @DeleteMapping("/deleteProduct/{orderId}/{itemId}")
  @Operation(
      operationId = "deleteProductFromOrder",
      summary = "Delete product from order",
      description = "Remove a specific product from an order"
  )
  public void deleteProduct(@PathVariable int orderId, @PathVariable int itemId) {
    accessVoter.assertCanDeleteProduct();
    orderDeleteProductUseCase.execute(orderId, itemId);
  }

  /**
   * Get orders by period.
   */
  @PostMapping("/getOrdersByPeriod")
  @Operation(
      operationId = "getOrdersByPeriod",
      summary = "Get orders by period",
      description = "Retrieve paginated orders filtered by date range and client"
  )
  public OrderListResponse getOrdersByPeriod(@Valid @RequestBody @NonNull OrderListRequest request) {
    accessVoter.assertCanGetOrdersByPeriod();
    return orderSearchByPeriodUseCase.execute(request);
  }

  /**
   * Update order.
   */
  @PutMapping("/update")
  @Operation(
      operationId = "updateOrder",
      summary = "Update order",
      description = "Update the quantity of a product in an order"
  )
  public void update(@Valid @RequestBody @NonNull UpdateOrderRequest request) {
    accessVoter.assertCanUpdate();
    orderUpdateUseCase.execute(request);
  }

  /**
   * Place order.
   */
  @PutMapping("/placeOrder/{orderId}")
  @Operation(
      operationId = "placeOrder",
      summary = "Place order",
      description = "Change order status from draft to placed"
  )
  public void placeOrder(@PathVariable int orderId) {
    accessVoter.assertCanPlaceOrder();
    orderPlaceUseCase.execute(orderId);
  }
}