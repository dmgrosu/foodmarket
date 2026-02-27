package md.ramaiana.foodmarket.domain.order.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.order.core.request.AddProductToOrderRequest;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderAddProductUseCaseTest extends BaseTest {

  @Autowired
  private OrderAddProductUseCase orderAddProductUseCase;

  @Autowired
  private OrderRepository orderRepository;

  @Test
  void whenNewOrder_thenCreatesOrderWithItem() {
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create(brand.getId(), group.getId());

    AddProductToOrderRequest request = new AddProductToOrderRequest(
        0, product.getId(), 3f, client.getId()
    );

    OrderResponse response = orderAddProductUseCase.execute(request);

    assertThat(response).isNotNull();
    assertThat(response.getClientId()).isEqualTo(client.getId());
    assertThat(response.getState()).isEqualTo(OrderState.NEW);
    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().getFirst().getQuantity()).isEqualTo(3f);
    assertThat(response.getItems().getFirst().getPrice()).isEqualTo(product.getPrice());
  }

  @Test
  void whenExistingOrder_thenAddsItemToOrder() {
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product1 = testProductService.create(brand.getId(), group.getId());
    ProductEntity product2 = testProductService.create(brand.getId(), group.getId());
    OrderEntity order = testOrderService.createWithItems(client.getId(), product1.getId());

    AddProductToOrderRequest request = new AddProductToOrderRequest(
        order.getId(), product2.getId(), 2f, client.getId()
    );

    OrderResponse response = orderAddProductUseCase.execute(request);

    assertThat(response.getItems()).hasSize(2);
    assertThat(response.getId()).isEqualTo(order.getId());
  }

  @Test
  void whenProductNotFound_thenThrowsNotFound() {
    ClientEntity client = testClientService.create();

    AddProductToOrderRequest request = new AddProductToOrderRequest(
        0, 999, 1f, client.getId()
    );

    assertThatThrownBy(() -> orderAddProductUseCase.execute(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Product");
  }

  @Test
  void whenClientNotFound_thenThrowsNotFound() {
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create(brand.getId(), group.getId());

    AddProductToOrderRequest request = new AddProductToOrderRequest(
        0, product.getId(), 1f, 999
    );

    assertThatThrownBy(() -> orderAddProductUseCase.execute(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Client");
  }

  @Test
  void whenOrderPlaced_thenThrowsBadRequest() {
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create(brand.getId(), group.getId());
    OrderEntity order = testOrderService.create(client.getId());
    order.setState(OrderState.PLACED);
    orderRepository.save(order);

    AddProductToOrderRequest request = new AddProductToOrderRequest(
        order.getId(), product.getId(), 1f, client.getId()
    );

    assertThatThrownBy(() -> orderAddProductUseCase.execute(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("placed");
  }
}
