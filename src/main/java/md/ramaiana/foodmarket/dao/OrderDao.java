package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kirill Grosu (grosukirill009@gmail.com), 2/11/2021
 */

@Repository
public interface OrderDao extends CrudRepository<Order, Integer> {
}
