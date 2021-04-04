package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.Good;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/10/21
 */
@Repository
public interface GoodDao extends PagingAndSortingRepository<Good, Integer> {

    List<Good> getAllByGroupIdAndBrandIdAndNameContainingAndDeletedAtNull(Integer groupId, Integer brandId, String name);

    List<Good> getAllByGroupIdAndBrandIdAndDeletedAtNull(Integer groupId, Integer brandId);

    List<Good> getAllByGroupIdAndNameContainingAndDeletedAtNull(Integer groupId, String name);

    List<Good> getAllByBrandIdAndNameContainingAndDeletedAtNull(Integer brandId, String name);

    List<Good> getAllByGroupIdAndDeletedAtNull(Integer groupId);

    List<Good> getAllByBrandIdAndDeletedAtNull(Integer brandId);

    List<Good> getAllByNameContainingAndDeletedAtNull(String name);

    List<Good> getAllByGroupIdNullAndDeletedAtNull();

    Optional<Good> findByErpCode(String erpCode);

    @Modifying
    @Query("update good set deleted_at = now() where id not in (:ids)")
    Integer setDeletedIfIdNotIn(@Param("ids") List<Integer> updatedGoodIds);

    Good getByIdAndDeletedAtNull(Integer goodId);
}
