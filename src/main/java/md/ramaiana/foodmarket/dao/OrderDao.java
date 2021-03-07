package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Kirill Grosu (grosukirill009@gmail.com), 2/11/2021
 */

@Repository
public interface OrderDao extends CrudRepository<Order, Integer> {
    List<Order> getAllByClientIdAndDeletedAtNull(Integer clientId);

    Order getByIdAndDeletedAtNull(Integer orderId);

    void deleteByIdAndProcessedAtNull(Integer orderId);

}
