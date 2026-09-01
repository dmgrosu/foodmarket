package md.ramaiana.foodmarket.domain.product.data;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Product Repository.
 */
public interface ProductRepository extends CrudRepository<ProductEntity, Integer>, ProductRepositoryCustom {

    Optional<ProductEntity> findByIdAndDeletedAtIsNull(Integer id);

    @Query("SELECT name FROM product WHERE id = :id")
    String findNameById(@Param("id") Integer id);

    Optional<ProductEntity> findByErpCode(String erpCode);
}
