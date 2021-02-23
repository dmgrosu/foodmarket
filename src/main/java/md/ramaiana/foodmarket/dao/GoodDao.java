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

    List<Good> getAllByGroupIdAndBrandIdAndNameLikeAndDeletedAtNull(Integer groupId, Integer brandId, String name);

    List<Good> getAllByGroupIdAndBrandIdAndDeletedAtNull(Integer groupId, Integer brandId);

    List<Good> getAllByGroupIdAndNameAndDeletedAtNull(Integer groupId, String name);

    List<Good> getAllByBrandIdAndNameAndDeletedAtNull(Integer brandId, String name);

    List<Good> getAllByGroupIdAndDeletedAtNull(Integer groupId);

    List<Good> getAllByBrandIdAndDeletedAtNull(Integer brandId);

    List<Good> getAllByNameAndDeletedAtNull(String name);

    List<Good> getAllByGroupIdNullAndDeletedAtNull();

}
