package md.ramaiana.foodmarket.domain.order.core.usecase;

import java.util.List;
import java.util.Map;
import java.util.Set;
import md.ramaiana.foodmarket.domain.order.core.response.OrderItemResponse;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.product.data.ProductRepository;
import md.ramaiana.foodmarket.shared.enums.PriceType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
class OrderResponseAssemblerTest {

  private final ProductRepository productRepository = mock(ProductRepository.class);
  private final OrderResponseAssembler assembler = new OrderResponseAssembler(productRepository);

  @Test
  void should_read_every_name_a_page_of_orders_mentions_in_one_query() {
    when(productRepository.findNamesByIds(any())).thenReturn(Map.of(1, "Cola", 2, "Bread"));

    List<OrderResponse> responses = assembler.assembleAll(List.of(
        orderWith(1, 2), orderWith(2), orderWith(1)));

    // The point of the assembler: three orders and four lines, still one lookup.
    ArgumentCaptor<java.util.Collection<Integer>> ids = ArgumentCaptor.captor();
    verify(productRepository, times(1)).findNamesByIds(ids.capture());
    assertThat(ids.getValue()).containsExactlyInAnyOrder(1, 2);
    assertThat(responses).hasSize(3);
  }

  @Test
  void should_still_render_a_line_whose_product_has_since_been_deleted() {
    // findNamesByIds filters on deleted_at, so a product removed after it was ordered resolves to
    // nothing. A historical order still has to render rather than fail.
    when(productRepository.findNamesByIds(any())).thenReturn(Map.of(1, "Cola"));

    OrderResponse response = assembler.assemble(orderWith(1, 99));

    assertThat(response.getItems())
        .extracting(OrderItemResponse::getProductId, OrderItemResponse::getProductName)
        .containsExactlyInAnyOrder(
            org.assertj.core.groups.Tuple.tuple(1, "Cola"),
            org.assertj.core.groups.Tuple.tuple(99, ""));
  }

  @Test
  void should_ask_for_nothing_when_the_order_is_empty() {
    when(productRepository.findNamesByIds(any())).thenReturn(Map.of());

    OrderResponse response = assembler.assemble(new OrderEntity(7, 1, PriceType.LOCAL));

    assertThat(response.getItems()).isEmpty();
    assertThat(response.getTotalSum()).isZero();
  }

  @Test
  void should_order_lines_by_product_name_so_a_cart_does_not_reshuffle_between_reads() {
    when(productRepository.findNamesByIds(any())).thenReturn(Map.of(1, "Cola", 2, "Bread"));

    OrderResponse response = assembler.assemble(orderWith(1, 2));

    assertThat(response.getItems())
        .extracting(OrderItemResponse::getProductName)
        .containsExactly("Bread", "Cola");
  }

  /**
   * The items live in a HashSet with no equals/hashCode, so their iteration order is neither the
   * insertion order nor stable across saves.
   */
  private OrderEntity orderWith(Integer... productIds) {
    OrderEntity order = new OrderEntity(7, 1, PriceType.LOCAL);
    for (Integer productId : Set.of(productIds)) {
      order.addProduct(productId, 10f, 1f, 1f);
    }
    return order;
  }
}
