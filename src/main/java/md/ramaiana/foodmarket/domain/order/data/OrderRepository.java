package md.ramaiana.foodmarket.domain.order.data;

import java.time.Instant;
import java.util.Optional;
import md.ramaiana.foodmarket.shared.enums.OrderState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Order Repository.
 */
public interface OrderRepository extends CrudRepository<OrderEntity, Integer>,
    PagingAndSortingRepository<OrderEntity, Integer> {

  Optional<OrderEntity> findByIdAndDeletedAtIsNull(Integer id);

  /**
   * The client's cart, which is their single {@link OrderState#NEW} order.
   */
  Optional<OrderEntity> findByClientIdAndStateAndDeletedAtIsNull(Integer clientId, OrderState state);

  Page<OrderEntity> findByClientIdAndDeletedAtIsNullAndCreatedAtBetween(
      Integer clientId,
      Instant from,
      Instant to,
      Pageable pageable
  );
}
