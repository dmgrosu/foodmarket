package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.Order;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Kirill Grosu (grosukirill009@gmail.com), 2/11/2021
 */

@Repository
public interface OrderDao extends PagingAndSortingRepository<Order, Integer> {

    Order getByIdAndDeletedAtNull(Integer orderId);

    @Modifying
    @Query("UPDATE \"order\" SET \"deleted_at\" = now() WHERE id = :orderId")
    void setOrderToDeletedState(@Param("orderId") Integer orderId);
}
