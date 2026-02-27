package md.ramaiana.foodmarket.domain.order.core.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import md.ramaiana.foodmarket.BaseTest;
import md.ramaiana.foodmarket.domain.client.data.ClientEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderPlaceUseCaseTest extends BaseTest {

  @Autowired
  private OrderPlaceUseCase orderPlaceUseCase;

  @Autowired
  private OrderRepository orderRepository;

  @Test
  void whenValidOrder_thenSetsStatePlaced() {
    ClientEntity client = testClientService.create();
    OrderEntity order = testOrderService.create(client.getId());

    orderPlaceUseCase.execute(order.getId());

    OrderEntity updated = orderRepository.findByIdAndDeletedAtIsNull(order.getId()).orElseThrow();
    assertThat(updated.getState()).isEqualTo(OrderState.PLACED);
  }

  @Test
  void whenOrderIdZero_thenThrowsBadRequest() {
    assertThatThrownBy(() -> orderPlaceUseCase.execute(0))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void whenOrderNotFound_thenThrowsNotFound() {
    assertThatThrownBy(() -> orderPlaceUseCase.execute(999))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("not found");
  }
}
