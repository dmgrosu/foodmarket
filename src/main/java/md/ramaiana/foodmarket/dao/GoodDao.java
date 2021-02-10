package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.Good;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Dmitri Grosu (dmitri.grosu@gmail.com), 2/10/21
 */
@Repository
public interface GoodDao extends CrudRepository<Good, Integer> {
}
