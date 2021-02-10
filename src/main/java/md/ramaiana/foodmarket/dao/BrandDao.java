package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.Brand;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/10/21
 */
@Repository
public interface BrandDao extends CrudRepository<Brand, Integer> {

    Optional<Brand> findByIdAndDeletedAtIsNull(Integer id);

    List<Brand> getAllByDeletedAtNull();

}
