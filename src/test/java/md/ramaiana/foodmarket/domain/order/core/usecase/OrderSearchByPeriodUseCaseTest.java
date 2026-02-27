package md.ramaiana.foodmarket.domain.order.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.brand.data.BrandEntity;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.order.core.request.OrderListRequest;
import md.ramaiana.foodmarket.domain.order.core.response.OrderListResponse;
import md.ramaiana.foodmarket.domain.product.data.ProductEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductGroupEntity;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

class OrderSearchByPeriodUseCaseTest extends BaseTest {

  @Autowired
  private OrderSearchByPeriodUseCase orderSearchByPeriodUseCase;

  @Test
  void whenOrdersInPeriod_thenReturnsPagedResults() {
    ClientEntity client = testClientService.create();
    BrandEntity brand = testBrandService.create();
    ProductGroupEntity group = testProductGroupService.create();
    ProductEntity product = testProductService.create(brand.getId(), group.getId());
    testOrderService.createWithItems(client.getId(), product.getId());
    testOrderService.createWithItems(client.getId(), product.getId());

    Instant now = Instant.now();
    OrderListRequest request = new OrderListRequest(
        now.minus(1, ChronoUnit.HOURS).toEpochMilli(),
        now.plus(1, ChronoUnit.HOURS).toEpochMilli(),
        client.getId(),
        0, 10,
        Sort.Direction.DESC, "createdAt"
    );

    OrderListResponse response = orderSearchByPeriodUseCase.execute(request);

    assertThat(response.getOrders()).hasSize(2);
    assertThat(response.getCurrentPage()).isZero();
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  void whenClientNotFound_thenThrowsNotFound() {
    Instant now = Instant.now();
    OrderListRequest request = new OrderListRequest(
        now.minus(1, ChronoUnit.HOURS).toEpochMilli(),
        now.plus(1, ChronoUnit.HOURS).toEpochMilli(),
        999,
        0, 10,
        Sort.Direction.DESC, "createdAt"
    );

    assertThatThrownBy(() -> orderSearchByPeriodUseCase.execute(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Client");
  }

  @Test
  void whenClientIdNull_thenThrowsBadRequest() {
    Instant now = Instant.now();
    OrderListRequest request = new OrderListRequest(
        now.minus(1, ChronoUnit.HOURS).toEpochMilli(),
        now.plus(1, ChronoUnit.HOURS).toEpochMilli(),
        null,
        0, 10,
        Sort.Direction.DESC, "createdAt"
    );

    assertThatThrownBy(() -> orderSearchByPeriodUseCase.execute(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("ClientId");
  }
}
