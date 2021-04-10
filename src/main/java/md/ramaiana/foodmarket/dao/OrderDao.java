package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;


/**
 * @author Kirill Grosu (grosukirill009@gmail.com), 2/11/2021
 */

@Repository
public interface OrderDao extends PagingAndSortingRepository<Order, Integer> {

    Optional<Order> findByIdAndDeletedAtNull(Integer orderId);

    @Modifying
    @Query("UPDATE \"order\" SET \"deleted_at\" = now() WHERE id = :orderId")
    void setOrderToDeletedState(@Param("orderId") Integer orderId);

    Page<Order> findAllByDeletedAtNullAndCreatedAtBetweenAndClientId(Pageable pageable, OffsetDateTime dateFrom, OffsetDateTime dateTo, Integer clientId);

    @Query("select \"processing_result\" from \"order\" where id = :orderId")
    String getProcessingResultById(@Param("orderId") Integer orderId);

    boolean existsByIdAndDeletedAtNull(Integer orderId);

}
