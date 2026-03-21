package md.ramaiana.foodmarket.domain.product.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Product Repository.
 */
public interface ProductRepository extends CrudRepository<ProductEntity, Integer> {

  Optional<ProductEntity> findByIdAndDeletedAtIsNull(Integer id);

  @Query("SELECT * FROM product WHERE deleted_at IS NULL"
      + " AND (:groupId IS NULL OR group_id = :groupId)"
      + " AND (:brandId IS NULL OR brand_id = :brandId)"
      + " AND (:nameLike IS NULL OR LOWER(name) LIKE :nameLike)")
  List<ProductEntity> findAllByFilters(
      @Param("groupId") Integer groupId,
      @Param("brandId") Integer brandId,
      @Param("nameLike") String nameLike
  );

  @Query("SELECT name FROM product WHERE id = :id")
  String findNameById(@Param("id") Integer id);

  Optional<ProductEntity> findByErpCode(String erpCode);
}
