package md.ramaiana.foodmarket.domain.product.data;

import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Product Repository.
 */
@Repository
public interface ProductRepository extends
    JpaRepository<@NonNull ProductEntity, @NonNull Integer>,
    JpaSpecificationExecutor<@NonNull ProductEntity> {

  /**
   * Specification for products that are not deleted.
   */
  @NonNull
  static Specification<@NonNull ProductEntity> notDeleted() {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.isNull(root.get("deletedAt"));
  }

  /**
   * Specification for products with a specific group ID.
   */
  @NonNull
  static Specification<@NonNull ProductEntity> groupIdEquals(@NonNull Integer groupId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("group").get("id"), groupId);
  }

  /**
   * Specification for products with a specific brand ID.
   */
  @NonNull
  static Specification<@NonNull ProductEntity> brandIdEquals(@NonNull Integer brandId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("brand").get("id"), brandId);
  }

  /**
   * Specification for products with name containing the given string (case-insensitive).
   */
  @NonNull
  static Specification<@NonNull ProductEntity> nameContainsIgnoreCase(@NonNull String name) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.like(
            criteriaBuilder.lower(root.get("name")),
            "%" + name.toLowerCase() + "%"
        );
  }

  /**
   * Specification for products with a specific ID.
   */
  @NonNull
  static Specification<@NonNull ProductEntity> idEquals(@NonNull Integer id) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("id"), id);
  }

  @Query("SELECT p.name FROM ProductEntity p WHERE p.id = :id")
  String findNameById(@NonNull @Param("id") Integer id);
}