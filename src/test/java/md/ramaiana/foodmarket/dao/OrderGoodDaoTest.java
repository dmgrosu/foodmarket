package md.ramaiana.foodmarket.dao;


import md.ramaiana.foodmarket.config.DataJdbcConfig;
import md.ramaiana.foodmarket.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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
        // ARRANGE
        Order someExistingOrder = someExistingOrder();
        Good someExistingGood = someExistingGood();
        // ACT
        OrderGood saved = orderGoodDao.save(OrderGood.builder()
                .orderId(someExistingOrder.getId())
                .goodId(someExistingGood.getId())
                .quantity(1000f)
                .sum(10000f)
                .weight(50f)
                .build());
        // ASSERT
        assertThat(orderGoodDao.existsById(saved.getId())).isTrue();
    }

    @Test
    void test_read() {
        // ARRANGE
        OrderGood someExistingOrderGood = someExistingOrderGood();
        // ACT
        boolean exists = orderGoodDao.existsById(someExistingOrderGood.getId());
        // ASSERT
        assertThat(exists).isTrue();
    }

    @Test
    void test_update() {
        // ARRANGE
        OrderGood someExistingOrderGood = someExistingOrderGood();
        // ACT
        someExistingOrderGood.setWeight(150f);
        OrderGood updatedOrder = orderGoodDao.save(someExistingOrderGood);
        // ASSERT
        assertThat(updatedOrder.getWeight() == 150f).isTrue();
    }

    @Test
    void test_delete() {
        // ARRANGE
        OrderGood someExistingGood = someExistingOrderGood();
        // ACT
        orderGoodDao.deleteById(someExistingGood.getId());
        // ASSERT
        assertThat(orderGoodDao.existsById(someExistingGood.getId())).isFalse();

    }

    @Test
    void test_countByOrder_countReturned() {
        // ARRANGE
        OrderGood someExistingGood = someExistingOrderGood();
        // ACT
        int actualResult = orderGoodDao.countAllByOrderId(someExistingGood.getOrderId());
        // ASSERT
        assertThat(actualResult).isEqualTo(1);
    }

    private Client someSavedClient(String idno, String name) {
        return clientDao.save(Client.builder()
                .idno(idno)
                .name(name)
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private Order someExistingOrder() {
        Client someSavedClient = someSavedClient("123123", "Kirill");
        return orderDao.save(Order.builder()
                .clientId(someSavedClient.getId())
                .createdAt(OffsetDateTime.now())
                .deletedAt(null)
                .processedAt(OffsetDateTime.now())
                .processingResult("Done")
                .state(OrderState.PLACED)
                .totalSum(150.3f)
                .build());
    }

    private Good someExistingGood() {
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

    private OrderGood someExistingOrderGood() {
        Good someExistingGood = someExistingGood();
        Order someExistingOrder = someExistingOrder();
        return orderGoodDao.save(OrderGood.builder()
                .orderId(someExistingOrder.getId())
                .goodId(someExistingGood.getId())
                .quantity(1000f)
                .sum(10000f)
                .weight(50f)
                .build());
    }
}
