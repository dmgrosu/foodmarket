package md.ramaiana.foodmarket.domain.product.data;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Sort;

/**
 * Product Group Repository.
 */
public interface ProductGroupRepository extends CrudRepository<ProductGroupEntity, Integer> {

  List<ProductGroupEntity> findByParentGroupIdAndDeletedAtIsNull(Integer parentGroupId, Sort sort);

  List<ProductGroupEntity> findByParentGroupIdIsNullAndDeletedAtIsNull(Sort sort);

  boolean existsByParentGroupId(Integer parentGroupId);

  Optional<ProductGroupEntity> findByErpCode(String erpCode);

  /**
   * Every group worth showing for a storage: the ones holding a product that is in stock, plus every
   * ancestor above them, so a folder whose contents are all further down still appears.
   * <p>
   * Returned as entities rather than ids because the caller needs both halves of each row - the id
   * to decide whether a group at the level being rendered is shown at all, and the parent id to
   * decide whether it is worth offering an expander.
   */
  @Query("""
      WITH RECURSIVE visible(id) AS (
          SELECT p.group_id
          FROM product p
          JOIN balances b ON b.product_id = p.id
          WHERE p.deleted_at IS NULL
            AND p.group_id IS NOT NULL
            AND b.quantity > 0
            AND (:storageId IS NULL OR b.storage_id = :storageId)
          UNION
          SELECT g.parent_group_id
          FROM product_group g
          JOIN visible v ON v.id = g.id
          WHERE g.parent_group_id IS NOT NULL
      )
      SELECT pg.* FROM product_group pg
      JOIN visible v ON v.id = pg.id
      WHERE pg.deleted_at IS NULL
      """)
  Set<ProductGroupEntity> findVisible(@Param("storageId") Integer storageId);
}
