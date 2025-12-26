package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.ProductGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/10/21
 */
@Repository
public interface ProductGroupDao extends PagingAndSortingRepository<ProductGroup, Integer>, CrudRepository<ProductGroup, Integer> {

    List<ProductGroup> getAllByParentGroupIdNullAndDeletedAtNullOrderByName();

    List<ProductGroup> getAllByParentGroupIdAndDeletedAtNullOrderByName(Integer groupId);

    Optional<ProductGroup> findByErpCode(String parentErp);

    List<ProductGroup> findByParentGroupIdNullAndDeletedAtNullOrderByName();

    List<ProductGroup> findByParentGroupIdAndDeletedAtNullOrderByName(Integer parentGroupId);

    boolean existsByParentGroupId(Integer parentGroupId);

}
