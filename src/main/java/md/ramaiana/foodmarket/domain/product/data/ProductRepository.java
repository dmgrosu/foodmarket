package md.ramaiana.foodmarket.domain.product.data;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

/**
 * Product Repository.
 */
public interface ProductRepository extends CrudRepository<ProductEntity, Integer> {

    Optional<ProductEntity> findByIdAndDeletedAtIsNull(Integer id);

    @Query("SELECT * " +
            "FROM product p " +
            "JOIN balances b on b.product_id = p.id " +
            "WHERE p.deleted_at IS NULL " +
            "AND (:groupId IS NULL OR p.group_id = :groupId) " +
            "AND (:brandId IS NULL OR p.brand_id = :brandId) " +
            "AND (:name IS NULL OR LOWER(p.name) LIKE :name)" +
            "AND b.quantity > 0 " +
            "AND (:storageId IS NULL OR b.storage_id = :storageId) ")
    Set<ProductEntity> findAllByFiltersHavingPositiveBalance(
            @Param("storageId") Integer storageId,
            @Param("groupId") Integer groupId,
            @Param("brandId") Integer brandId,
            @Param("name") String name
    );

    @Query("SELECT name FROM product WHERE id = :id")
    String findNameById(@Param("id") Integer id);

    Optional<ProductEntity> findByErpCode(String erpCode);
}
