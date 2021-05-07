package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.OrderGood;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Kirill Grosu (grosukirill009@gmail.com), 2/11/2021
 */

@Repository
public interface OrderGoodDao extends CrudRepository<OrderGood, Integer> {

    @Query("UPDATE \"order_good\" SET \"quantity\" := newQuantity WHERE \"order_id\" := orderId AND \"good_id\" := goodId")
    @Modifying
    void updateOrderGoodQuantity(@Param("orderId") Integer orderId,
                                 @Param("goodId") Integer goodId,
                                 @Param("newQuantity") Float newQuantity);

    boolean existsByOrderIdAndGoodId(Integer orderId, Integer goodId);

    int countAllByOrderId(int orderId);

}
