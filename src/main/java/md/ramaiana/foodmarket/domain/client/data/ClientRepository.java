package md.ramaiana.foodmarket.domain.client.data;

import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Client Repository.
 */
@Repository
public interface ClientRepository extends JpaRepository<@NonNull ClientEntity, @NonNull Integer>, JpaSpecificationExecutor<@NonNull ClientEntity> {

  /**
   * Specification for finding {@link ClientEntity} by idno.
   */
  @NonNull
  static Specification<@NonNull ClientEntity> idnoEquals(@NonNull String input) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("idno"), input);
  }

  /**
   * Specification for finding non-deleted {@link ClientEntity}.
   */
  @NonNull
  static Specification<@NonNull ClientEntity> notDeleted() {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.isNull(root.get("deletedAt"));
  }
}
