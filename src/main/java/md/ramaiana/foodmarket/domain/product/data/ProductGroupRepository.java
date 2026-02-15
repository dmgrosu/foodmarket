package md.ramaiana.foodmarket.domain.product.data;

import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Product Group Repository.
 */
@Repository
public interface ProductGroupRepository extends
    JpaRepository<@NonNull ProductGroupEntity, @NonNull Integer>,
    JpaSpecificationExecutor<@NonNull ProductGroupEntity> {

  /**
   * Specification for product groups that are not deleted.
   */
  @NonNull
  static Specification<@NonNull ProductGroupEntity> notDeleted() {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.isNull(root.get("deletedAt"));
  }

  /**
   * Specification for product groups with null parent group.
   */
  @NonNull
  static Specification<@NonNull ProductGroupEntity> parentGroupIsNull() {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.isNull(root.get("parentGroup"));
  }

  /**
   * Specification for product groups with a specific parent group ID.
   */
  @NonNull
  static Specification<@NonNull ProductGroupEntity> parentGroupIdEquals(@NonNull Integer parentGroupId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("parentGroup").get("id"), parentGroupId);
  }

  boolean existsByParentGroupId(Integer parentGroupId);
}