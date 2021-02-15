package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.GoodGroup;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/10/21
 */
@Repository
public interface GoodGroupDao extends PagingAndSortingRepository<GoodGroup, Integer> {

    List<GoodGroup> getAllByParentGroupIdNullAndDeletedAtNull();
}
