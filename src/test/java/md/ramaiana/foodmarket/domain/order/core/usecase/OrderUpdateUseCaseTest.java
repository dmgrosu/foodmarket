package md.ramaiana.foodmarket.domain.order.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.order.core.request.UpdateOrderRequest;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderItemEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderUpdateUseCaseTest extends BaseTest {

  @Autowired
  private OrderUpdateUseCase orderUpdateUseCase;

  @Autowired
  private OrderRepository orderRepository;

  @Test
  void whenValidUpdate_thenRecalculatesSumAndWeight() {
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create("Heavy Item", 100f, 2.0f, brand.getId(), group.getId());
    OrderEntity order = testOrderService.createWithItems(client.getId(), product.getId());

    UpdateOrderRequest request = new UpdateOrderRequest(order.getId(), product.getId(), 5f);
    orderUpdateUseCase.execute(request);

    OrderEntity updated = orderRepository.findByIdAndDeletedAtIsNull(order.getId()).orElseThrow();
    OrderItemEntity item = updated.getItems().iterator().next();
    assertThat(item.getQuantity()).isEqualTo(5f);
    assertThat(item.getSum()).isEqualTo(500f); // 100 * 5
    assertThat(item.getWeight()).isEqualTo(10f); // 2.0 * 5
    assertThat(updated.getTotalSum()).isEqualTo(500f);
  }

  @Test
  void whenProductNotInOrder_thenThrowsNotFound() {
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create(brand.getId(), group.getId());
    OrderEntity order = testOrderService.createWithItems(client.getId(), product.getId());

    UpdateOrderRequest request = new UpdateOrderRequest(order.getId(), 999, 5f);

    assertThatThrownBy(() -> orderUpdateUseCase.execute(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("not found in order");
  }

  @Test
  void whenOrderNotFound_thenThrowsNotFound() {
    UpdateOrderRequest request = new UpdateOrderRequest(999, 1, 5f);

    assertThatThrownBy(() -> orderUpdateUseCase.execute(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Order");
  }
}
