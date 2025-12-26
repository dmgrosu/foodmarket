package md.ramaiana.foodmarket.dao;

import lombok.NonNull;
import md.ramaiana.foodmarket.model.Product;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductDao extends CrudRepository<@NonNull Product, @NonNull Integer>,
        PagingAndSortingRepository<@NonNull Product, @NonNull Integer> {

    List<Product> getAllByGroupIdAndBrandIdAndNameIgnoreCaseContainingAndDeletedAtNull(Integer groupId, Integer brandId, String name);

    List<Product> getAllByGroupIdAndBrandIdAndDeletedAtNull(Integer groupId, Integer brandId);

    List<Product> getAllByGroupIdAndNameIgnoreCaseContainingAndDeletedAtNull(Integer groupId, String name);

    List<Product> getAllByBrandIdAndNameIgnoreCaseContainingAndDeletedAtNull(Integer brandId, String name);

    List<Product> getAllByGroupIdAndDeletedAtNull(Integer groupId);

    List<Product> getAllByBrandIdAndDeletedAtNull(Integer brandId);

    List<Product> getAllByNameIgnoreCaseContainingAndDeletedAtNull(String name);

    List<Product> getAllByGroupIdNullAndDeletedAtNull();

    Optional<Product> findByErpCode(String erpCode);

    @Modifying
    @Query("update Product set deleted_at = now() where id not in (:ids)")
    Integer setDeletedIfIdNotIn(@Param("ids") List<Integer> updatedProductIds);

    Optional<Product> findByIdAndDeletedAtNull(Integer productId);

    @Query("select name from Product where id=:productId")
    String getNameById(@Param("productId") Integer productId);
}
