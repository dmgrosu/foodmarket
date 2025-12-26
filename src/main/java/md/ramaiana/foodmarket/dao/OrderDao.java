package md.ramaiana.foodmarket.dao;

import lombok.NonNull;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;


public interface OrderDao extends PagingAndSortingRepository<Order, Integer>, CrudRepository<Order, Integer> {

    Optional<Order> findByIdAndDeletedAtNull(Integer orderId);

    @Modifying
    @Query("UPDATE \"order\" SET \"deleted_at\" = now() WHERE id = :orderId")
    void setOrderToDeletedState(@Param("orderId") Integer orderId);

    Page<@NonNull Order> findAllByDeletedAtNullAndCreatedAtBetweenAndClient(Pageable pageable, OffsetDateTime dateFrom, OffsetDateTime dateTo, Integer clientId);

    @Query("select \"processing_result\" from \"order\" where id = :orderId")
    String getProcessingResultById(@Param("orderId") Integer orderId);

    boolean existsByIdAndDeletedAtNull(Integer orderId);

    @Modifying
    @Query("update \"order\" set \"status\"=:state where id=:orderId")
    void updateOrderState(@Param("state") OrderState state, @Param("orderId") int orderId);

}
