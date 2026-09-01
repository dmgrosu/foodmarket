package md.ramaiana.foodmarket.domain.order.core.usecase;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.core.response.OrderResponse;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.annotation.UseCase;
import md.ramaiana.foodmarket.shared.exception.http.BadRequestException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for placing the caller's cart as an order.
 * <p>
 * Placing is the hand-off point: from here the order is the ERP's to fulfil, and nothing in the
 * cart API will touch it again. It is picked up for export by {@code ExportOrdersUseCase} on the
 * next scheduled cycle.
 */
@UseCase
@RequiredArgsConstructor
public class OrderPlaceUseCase {

  private final OrderRepository orderRepository;
  private final OrderLoader orderLoader;
  private final OrderResponseAssembler responseAssembler;

  /**
   * Execute the use case.
   *
   * @throws BadRequestException if the cart holds nothing. Requiring a line here is what stops an
   *                             empty order reaching the ERP, since only the cart state is
   *                             reachable from this endpoint and an empty one carries no intent.
   */
  @NonNull
  @Transactional(rollbackFor = Exception.class)
  public OrderResponse execute() {
    OrderEntity cart = orderLoader.requireCart();

    if (cart.isEmpty()) {
      throw new BadRequestException("Cannot place an empty order");
    }

    cart.place();

    return responseAssembler.assemble(orderRepository.save(cart));
  }
}
