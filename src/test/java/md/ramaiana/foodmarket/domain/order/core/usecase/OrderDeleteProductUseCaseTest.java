package md.ramaiana.foodmarket.domain.order.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderDeleteProductUseCaseTest extends BaseTest {

  @Autowired
  private OrderDeleteProductUseCase orderDeleteProductUseCase;

  @Autowired
  private OrderRepository orderRepository;

  @Test
  void whenItemExists_thenRemovesFromOrder() {
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create(brand.getId(), group.getId());
    OrderEntity order = testOrderService.createWithItems(client.getId(), product.getId());

    Integer itemId = order.getItems().iterator().next().getId();
    orderDeleteProductUseCase.execute(order.getId(), itemId);

    OrderEntity updated = orderRepository.findByIdAndDeletedAtIsNull(order.getId()).orElseThrow();
    assertThat(updated.getItems()).isEmpty();
    assertThat(updated.getTotalSum()).isEqualTo(0f);
  }

  @Test
  void whenItemNotFound_thenThrowsNotFound() {
    ClientEntity client = testClientService.create();
    OrderEntity order = testOrderService.create(client.getId());

    assertThatThrownBy(() -> orderDeleteProductUseCase.execute(order.getId(), 999))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("item");
  }

  @Test
  void whenItemIdZero_thenThrowsBadRequest() {
    ClientEntity client = testClientService.create();
    OrderEntity order = testOrderService.create(client.getId());

    assertThatThrownBy(() -> orderDeleteProductUseCase.execute(order.getId(), 0))
        .isInstanceOf(BadRequestException.class);
  }
}
