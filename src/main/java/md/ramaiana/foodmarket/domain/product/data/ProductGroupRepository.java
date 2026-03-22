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

  @Query("SELECT DISTINCT pg.* FROM product_group pg " +
          "JOIN product p ON p.group_id = pg.id " +
          "JOIN balances b ON b.product_id = p.id " +
          "WHERE pg.deleted_at IS NULL " +
          "AND p.deleted_at IS NULL " +
          "AND b.quantity > 0 " +
          "AND (:storageId IS NULL OR b.storage_id = :storageId)")
  Set<ProductGroupEntity> findAllNonEmpty(@Param("storageId") Integer storageId);
}
