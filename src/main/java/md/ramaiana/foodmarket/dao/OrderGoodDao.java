package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.OrderGood;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kirill Grosu (grosukirill009@gmail.com), 2/11/2021
 */

@Repository
public interface OrderGoodDao extends CrudRepository<OrderGood, Integer> {

}
