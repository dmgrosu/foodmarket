package md.ramaiana.foodmarket.domain.order.data;

import java.time.Instant;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Order Repository.
 */
@Repository
public interface OrderRepository extends JpaRepository<@NonNull OrderEntity, @NonNull Integer>, JpaSpecificationExecutor<@NonNull OrderEntity> {

  /**
   * Specification for orders that are not deleted.
   */
  @NonNull
  static Specification<@NonNull OrderEntity> notDeleted() {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.isNull(root.get("deletedAt"));
  }

  /**
   * Specification for orders with a specific ID.
   */
  @NonNull
  static Specification<@NonNull OrderEntity> idEquals(@NonNull Integer id) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("id"), id);
  }

  /**
   * Specification for orders created within a date range.
   */
  @NonNull
  static Specification<@NonNull OrderEntity> createdAtBetween(@NonNull Instant startDate, @NonNull Instant endDate) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
  }

  /**
   * Specification for orders belonging to a specific client.
   */
  @NonNull
  static Specification<@NonNull OrderEntity> clientIdEquals(@NonNull Integer clientId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("client").get("id"), clientId);
  }
}
