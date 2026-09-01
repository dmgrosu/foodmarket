package md.ramaiana.foodmarket.domain.order.core.usecase;

import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import md.ramaiana.foodmarket.domain.order.data.OrderEntity;
import md.ramaiana.foodmarket.domain.order.data.OrderRepository;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import md.ramaiana.foodmarket.shared.exception.http.NotFoundException;
import md.ramaiana.foodmarket.shared.util.CurrentUserProvider;
import org.springframework.stereotype.Component;

/**
 * Resolves the orders the caller is allowed to touch.
 * <p>
 * Not a use case, and deliberately the only place an order is fetched for an HTTP caller: "an order
 * belongs to the authenticated user's client" is a security rule, and a rule enforced in six use
 * cases is a rule that will one day be enforced in five. Order ids are sequential and guessable, so
 * before this existed any authenticated user could read or mutate any other client's order by id.
 * <p>
 * A foreign order is reported as {@link NotFoundException}, not {@code Forbidden}: telling a caller
 * that an order they cannot see nonetheless exists leaks the order book's size and activity.
 */
@Component
@RequiredArgsConstructor
public class OrderLoader {

  private final OrderRepository orderRepository;
  private final CurrentUserProvider currentUserProvider;

  /**
   * The caller's cart — their single {@link OrderState#NEW} order — if they have started one.
   */
  @NonNull
  public Optional<OrderEntity> findCart() {
    return orderRepository.findByClientIdAndStateAndDeletedAtIsNull(
        currentUserProvider.getCurrentClientId(), OrderState.NEW);
  }

  /**
   * The caller's cart, for an operation that has nothing to do without one.
   *
   * @throws NotFoundException if the caller has no open cart.
   */
  @NonNull
  public OrderEntity requireCart() {
    return findCart().orElseThrow(() -> new NotFoundException("No open cart"));
  }

  /**
   * A live order belonging to the caller's client.
   *
   * @throws NotFoundException if there is no such order, or it belongs to another client.
   */
  @NonNull
  public OrderEntity requireOwned(int orderId) {
    Integer clientId = currentUserProvider.getCurrentClientId();

    return orderRepository.findByIdAndDeletedAtIsNull(orderId)
        .filter(order -> clientId.equals(order.getClientId()))
        .orElseThrow(() -> new NotFoundException(String.format("Order with ID [%s] not found", orderId)));
  }

  @NonNull
  public Integer getCurrentClientId() {
    return currentUserProvider.getCurrentClientId();
  }
}
