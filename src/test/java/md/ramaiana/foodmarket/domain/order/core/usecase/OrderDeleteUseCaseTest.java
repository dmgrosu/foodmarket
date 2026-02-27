package md.ramaiana.foodmarket.domain.order.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderDeleteUseCaseTest extends BaseTest {

  @Autowired
  private OrderDeleteUseCase orderDeleteUseCase;

  @Autowired
  private OrderRepository orderRepository;

  @Test
  void whenOrderExists_thenSoftDeletes() {
    ClientEntity client = testClientService.create();
    OrderEntity order = testOrderService.create(client.getId());

    orderDeleteUseCase.execute(order.getId());

    assertThat(orderRepository.findByIdAndDeletedAtIsNull(order.getId())).isEmpty();
    assertThat(orderRepository.findById(order.getId())).isPresent();
  }

  @Test
  void whenOrderNotFound_thenThrowsNotFound() {
    assertThatThrownBy(() -> orderDeleteUseCase.execute(999))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("not found");
  }
}
