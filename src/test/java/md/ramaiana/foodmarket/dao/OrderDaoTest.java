package md.ramaiana.foodmarket.dao;

import md.ramaiana.foodmarket.config.DataJdbcConfig;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;

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
        Client someSavedClient = someSavedClient("123123", "Kirill");
        orderDao.save(Order.builder()
                .clientId(someSavedClient.getId())
                .createdAt(OffsetDateTime.now())
                .deletedAt(null)
                .processedAt(OffsetDateTime.now())
                .processingResult("done")
                .totalSum(150.3f)
                .build());
    }

    @Test
    void test_read() {
        Order existingOrder = someOrder();
        orderDao.existsById(existingOrder.getId());
    }

    @Test
    void test_update() {
        Order someOrder = someOrder();
        orderDao.save(someOrder);
        someOrder.setProcessingResult("Reviewing");
        someOrder.setProcessedAt(OffsetDateTime.now());
        orderDao.save(someOrder);
    }

    @Test
    void test_delete() {
        Order someOrder = someOrder();
        orderDao.deleteById(someOrder.getId());
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
