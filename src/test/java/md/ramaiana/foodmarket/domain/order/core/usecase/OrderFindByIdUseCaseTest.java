package md.ramaiana.foodmarket.domain.order.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderFindByIdUseCaseTest extends BaseTest {

  @Autowired
  private OrderFindByIdUseCase orderFindByIdUseCase;

  @Test
  void whenOrderExists_thenReturnsWithItems() {
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create(brand.getId(), group.getId());
    OrderEntity order = testOrderService.createWithItems(client.getId(), product.getId());

    OrderResponse response = orderFindByIdUseCase.execute(order.getId());

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(order.getId());
    assertThat(response.getClientId()).isEqualTo(client.getId());
    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().getFirst().getProductName()).isEqualTo(product.getName());
  }

  @Test
  void whenOrderNotFound_thenThrowsNotFound() {
    assertThatThrownBy(() -> orderFindByIdUseCase.execute(999))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void whenOrderIdZero_thenThrowsBadRequest() {
    assertThatThrownBy(() -> orderFindByIdUseCase.execute(0))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("zero");
  }
}
