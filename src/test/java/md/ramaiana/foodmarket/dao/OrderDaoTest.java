package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.config.DataJdbcConfig;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Order;
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
                .deletedAt(null)
                .processedAt(OffsetDateTime.now())
                .processingResult("done")
                .totalSum(150.3f)
                .build());
        // ASSERT
        assertThat(orderDao.existsById(saved.getId())).isTrue();
    }

    @Test
    void test_read() {
        // ARRANGE
        Order existingOrder = someOrder();
        // ACT
        boolean exists = orderDao.existsById(existingOrder.getId());
        // ASSERT
        assertThat(exists).isTrue();
    }

    @Test
    void test_update() {
        // ARRANGE
        Order someOrder = someOrder();
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
        Order someOrder = someOrder();
        // ACT
        orderDao.deleteById(someOrder.getId());
        // ASSERT
        assertThat(orderDao.existsById(someOrder.getId())).isFalse();
    }

    private Client someSavedClient(String idno, String name) {
        return clientDao.save(Client.builder()
                .idno(idno)
                .name(name)
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private Order someOrder() {
        Client someSavedClient = someSavedClient("123123", "Kirill");
        return orderDao.save(Order.builder()
                .clientId(someSavedClient.getId())
                .createdAt(OffsetDateTime.now())
                .deletedAt(null)
                .processedAt(OffsetDateTime.now())
                .processingResult("Done")
                .totalSum(150.3f)
                .build());
    }
}
