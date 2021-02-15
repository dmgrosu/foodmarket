package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.Good;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/10/21
 */
@Repository
public interface GoodDao extends PagingAndSortingRepository<Good, Integer> {

    List<Good> getAllByDeletedAtNull();

    List<Good> getAllByGroupIdAndBrandIdAndNameLike(Integer groupId, Integer brandId, String name);

    List<Good> getAllByGroupIdAndBrandId(Integer groupId, Integer brandId);

    List<Good> getAllByGroupIdAndName(Integer groupId, String name);

    List<Good> getAllByBrandIdAndName(Integer brandId, String name);

    List<Good> getAllByGroupId(Integer groupId);

    List<Good> getAllByBrandId(Integer brandId);

    List<Good> getAllByName(String name);

    List<Good> getAllByGroupIdNullAndDeletedAtNull();

}
