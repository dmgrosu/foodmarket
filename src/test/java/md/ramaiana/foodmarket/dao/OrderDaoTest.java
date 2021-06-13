package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.config.DataJdbcConfig;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderState;
import md.ramaiana.foodmarket.service.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Kirill Grosu (grosukirill009@gmail.com, 2/11/2021
 */
@DataJdbcTest
@Import(DataJdbcConfig.class)
class OrderDaoTest {
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private ClientDao clientDao;

    @Test
    void test_create() {
        // ARRANGE
        Client someSavedClient = someSavedClient("123123", "Kirill");
        // ACT
        Order saved = orderDao.save(Order.builder()
                .clientId(someSavedClient.getId())
                .createdAt(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .processingResult("done")
                .state(OrderState.NEW)
                .totalSum(150.3f)
                .build());
        // ASSERT
        assertThat(orderDao.existsById(saved.getId())).isTrue();
    }

    @Test
    void test_read() {
        // ARRANGE
        Order existingOrder = someOrder(OrderState.NEW);
        // ACT
        boolean exists = orderDao.existsById(existingOrder.getId());
        // ASSERT
        assertThat(exists).isTrue();
    }

    @Test
    void test_update() {
        // ARRANGE
        Order someOrder = someOrder(OrderState.NEW);
        // ACT
        someOrder.setProcessingResult("Reviewing");
        someOrder.setProcessedAt(OffsetDateTime.now());
        Order updatedOrder =  orderDao.save(someOrder);
        // ASSERT
        assertThat(updatedOrder.getProcessingResult().equals("Reviewing")).isTrue();
    }

    @Test
    void test_delete() {
        // ARRANGE
        Order someOrder = someOrder(OrderState.NEW);
        // ACT
        orderDao.deleteById(someOrder.getId());
        // ASSERT
        assertThat(orderDao.existsById(someOrder.getId())).isFalse();
    }

    @Test
    void test_updateOrderState() throws Exception {
        // ARRANGE
        Order givenOrder = someOrder(OrderState.NEW);
        int givenOrderId = givenOrder.getId();
        // ACT
        orderDao.updateOrderState(OrderState.PLACED, givenOrderId);
        Order actualOrder = orderDao.findById(givenOrderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        // ASSERT
        assertThat(actualOrder).isNotNull();
        assertThat(actualOrder.getState()).isEqualTo(OrderState.PLACED);
    }


//    @Test
//    void test_setOrderToDeletedState() {
//        //ARRANGE
//        Order someOrder = someOrder();
//        //ACT
//        orderDao.setOrderToDeletedState(someOrder.getId());
//        //ASSERT
//        Order updateOrder = orderDao.getById(someOrder.getId());
//        assertThat(updateOrder.getDeletedAt() != null).isTrue();
//    }

    private Client someSavedClient(String idno, String name) {
        return clientDao.save(Client.builder()
                .idno(idno)
                .name(name)
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private Order someOrder(OrderState orderState) {
        Client someSavedClient = someSavedClient("123123", "Kirill");
        return orderDao.save(Order.builder()
                .clientId(someSavedClient.getId())
                .createdAt(OffsetDateTime.now())
                .deletedAt(null)
                .processedAt(OffsetDateTime.now())
                .processingResult("Done")
                .state(orderState)
                .totalSum(150.3f)
                .build());
    }
}
