package md.ramaiana.foodmarket.domain.product.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

/**
 * Product Group Repository.
 */
public interface ProductGroupRepository extends CrudRepository<ProductGroupEntity, Integer> {

  List<ProductGroupEntity> findByParentGroupIdAndDeletedAtIsNull(Integer parentGroupId, Sort sort);

  List<ProductGroupEntity> findByParentGroupIdIsNullAndDeletedAtIsNull(Sort sort);

  boolean existsByParentGroupId(Integer parentGroupId);

  Optional<ProductGroupEntity> findByErpCode(String erpCode);
}
