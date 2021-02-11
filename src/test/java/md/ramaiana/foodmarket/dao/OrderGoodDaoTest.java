package md.ramaiana.foodmarket.dao;


import md.ramaiana.foodmarket.config.DataJdbcConfig;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderGood;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;

@DataJdbcTest
@Import(DataJdbcConfig.class)
class OrderGoodDaoTest {
    @Autowired
    private OrderGoodDao orderGoodDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private GoodDao goodDao;

    @Test
    void test_create() {
        Order someOrder = someOrder();
        Good someGood = someGood();
        orderGoodDao.save(OrderGood.builder()
                .orderId(someOrder.getId())
                .goodId(someGood.getId())
                .quantity(1000f)
                .sum(10000f)
                .weight(50f)
                .build());
    }

    @Test
    void test_read() {
        Good existingGood = someGood();
        orderGoodDao.existsById(existingGood.getId());
    }

    @Test
    void test_update() {
        Good someGood = someGood();
        goodDao.save(someGood);
        someGood.setName("Bread");
        goodDao.save(someGood);
    }

    @Test
    void test_delete() {
        Good someGood = someGood();
        goodDao.deleteById(someGood.getId());
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

    private Good someGood() {
        return goodDao.save(Good.builder()
                .name("Water")
                .price(10f)
                .unit("Liquids")
                .inPackage(5f)
                .erpCode("123412351")
                .barCode("qwerty")
                .weight(20f)
                .createdAt(OffsetDateTime.now())
                .build());
    }
}
