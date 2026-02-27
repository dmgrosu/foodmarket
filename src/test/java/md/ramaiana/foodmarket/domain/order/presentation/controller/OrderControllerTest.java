package md.ramaiana.foodmarket.domain.order.presentation.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import md.ramaiana.foodmarket.BaseControllerTest;
import md.ramaiana.foodmarket.domain.auth.data.AppUserEntity;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.order.core.request.AddProductToOrderRequest;
import md.ramaiana.foodmarket.domain.order.core.request.OrderListRequest;
import md.ramaiana.foodmarket.domain.order.core.request.UpdateOrderRequest;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class OrderControllerTest extends BaseControllerTest {

  @Test
  void whenAddProduct_thenReturnsOrder() throws Exception {
    AppUserEntity user = testAppUserService.create();
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create(brand.getId(), group.getId());

    doPost("/order/addProduct",
        new AddProductToOrderRequest(0, product.getId(), 2f, client.getId()),
        user)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.clientId").value(client.getId()))
        .andExpect(jsonPath("$.state").value(OrderState.NEW.name()))
        .andExpect(jsonPath("$.items.length()").value(1));
  }

  @Test
  void whenGetById_thenReturnsOrder() throws Exception {
    AppUserEntity user = testAppUserService.create();
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create(brand.getId(), group.getId());
    OrderEntity order = testOrderService.createWithItems(client.getId(), product.getId());

    doGet("/order/getById/" + order.getId(), user)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(order.getId()))
        .andExpect(jsonPath("$.items.length()").value(1));
  }

  @Test
  void whenDeleteById_thenSoftDeletes() throws Exception {
    AppUserEntity user = testAppUserService.create();
    ClientEntity client = testClientService.create();
    OrderEntity order = testOrderService.create(client.getId());

    doDelete("/order/deleteById/" + order.getId(), user)
        .andExpect(status().isOk());

    doGet("/order/getById/" + order.getId(), user)
        .andExpect(status().isNotFound());
  }

  @Test
  void whenDeleteProduct_thenRemovesItem() throws Exception {
    AppUserEntity user = testAppUserService.create();
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create(brand.getId(), group.getId());
    OrderEntity order = testOrderService.createWithItems(client.getId(), product.getId());
    Integer itemId = order.getItems().iterator().next().getId();

    doDelete("/order/deleteProduct/" + order.getId() + "/" + itemId, user)
        .andExpect(status().isOk());
  }

  @Test
  void whenUpdate_thenUpdatesQuantity() throws Exception {
    AppUserEntity user = testAppUserService.create();
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create(brand.getId(), group.getId());
    OrderEntity order = testOrderService.createWithItems(client.getId(), product.getId());

    doPut("/order/update",
        new UpdateOrderRequest(order.getId(), product.getId(), 10f),
        user)
        .andExpect(status().isOk());
  }

  @Test
  void whenPlaceOrder_thenSetsPlaced() throws Exception {
    AppUserEntity user = testAppUserService.create();
    ClientEntity client = testClientService.create();
    OrderEntity order = testOrderService.create(client.getId());

    doPut("/order/placeOrder/" + order.getId(), null, user)
        .andExpect(status().isOk());
  }

  @Test
  void whenSearchByPeriod_thenReturnsPaged() throws Exception {
    AppUserEntity user = testAppUserService.create();
    ClientEntity client = testClientService.create();
    testOrderService.create(client.getId());

    Instant now = Instant.now();
    OrderListRequest request = new OrderListRequest(
        now.minus(1, ChronoUnit.HOURS).toEpochMilli(),
        now.plus(1, ChronoUnit.HOURS).toEpochMilli(),
        client.getId(),
        0, 10,
        Sort.Direction.DESC, "createdAt"
    );

    doPost("/order/getOrdersByPeriod", request, user)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orders").isArray())
        .andExpect(jsonPath("$.totalPages").isNumber());
  }

  @Test
  void whenUnauthenticated_thenReturns403() throws Exception {
    doGet("/order/getById/1")
        .andExpect(status().isForbidden());
  }
}
