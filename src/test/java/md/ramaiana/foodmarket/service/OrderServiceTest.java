package md.ramaiana.foodmarket.service;


import md.ramaiana.foodmarket.dao.ClientDao;
import md.ramaiana.foodmarket.dao.GoodDao;
import md.ramaiana.foodmarket.dao.OrderDao;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;


@ExtendWith({SpringExtension.class})
public class OrderServiceTest {
    @Mock
    OrderDao orderDaoMock;

    @Mock
    GoodDao goodDaoMock;

    @Mock
    ClientDao clientDaoMock;

    @InjectMocks
    OrderService orderService;


    @Test
    void test_deleteOrder() {
        //ARRANGE
        Integer orderId = 1;
        //ACT
        orderService.deleteOrder(orderId);
        //ASSERT
        verify(orderDaoMock, times(1))
                .deleteByIdAndProcessedAtNull(orderId);
    }

    @Test
    void test_addGoodToOrder_responseOk() throws GoodNotFoundException, ClientNotFoundException {
        //ARRANGE
        Integer orderId = 2;
        Good someGood = Good.builder().id(1).price(15f).build();
        Float quantity = 5f;
        Integer clientId = 3;
        Float sum = someGood.getPrice() * quantity;
        when(goodDaoMock.getByIdAndDeletedAtNull(someGood.getId()))
                .thenReturn(someGood);
        when(clientDaoMock.getByIdAndDeletedAtNull(clientId))
                .thenReturn(Client.builder().id(3).build());

        //ACT
        orderService.addGoodToOrder(orderId, someGood.getId(), quantity, clientId);
        //ASSERT
        verify(orderDaoMock, times(1))
                .save(Order.builder()
                        .id(orderId)
                        .clientId(clientId)
                        .totalSum(sum)
                        .build());
    }

    @Test
    void test_addGoodToOrder_responseGoodValidationError_exceptionThrown() {
        //ARRANGE
        int goodId = 1;
        //ACT & ASSERT
        assertThatExceptionOfType(GoodNotFoundException.class)
                .isThrownBy(() -> orderService.addGoodToOrder(2, goodId, 15f, 5));
    }

    @Test
    void test_addGoodToOrder_responseClientValidationError_exceptionThrown() {
        //ARRANGE
        int clientId = 1;
        when(goodDaoMock.getByIdAndDeletedAtNull(6))
                .thenReturn(Good.builder().id(6).build());
        //ACT & ASSERT
        assertThatExceptionOfType(ClientNotFoundException.class)
                .isThrownBy(() -> orderService.addGoodToOrder(2, 6, 15f, clientId));
    }

    @Test
    void test_findOrderById_responseOk() throws Exception {
        //ARRANGE
        Integer orderId = 1;
        Order someOrder = Order.builder()
                .id(orderId)
                .build();
        when(orderDaoMock.getByIdAndDeletedAtNull(eq(orderId)))
                .thenReturn(someOrder);
        //ACT
        orderService.findOrdersById(someOrder.getId());
        //ASSERT
        verify(orderDaoMock, times(1))
                .getByIdAndDeletedAtNull(orderId);
    }

    @Test
    void test_findOrderById_throwsNotFoundException() {
        //ARRANGE
        Integer orderId = 1;
        //ACT & ASSERT
        assertThatExceptionOfType(OrderNotFoundException.class)
                .isThrownBy(() -> orderService.findOrdersById(orderId));
    }

    @Test
    void test_findOrderById_throwsIllegalArgumentException() {
        //ARRANGE
        Integer orderId = null;
        //ACT & ASSERT
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> orderService.findOrdersById(orderId));
    }

    @Test
    void test_findOrderById_throwsZeroException() {
        //ARRANGE
        Integer orderId = 0;
        //ACT & ASSERT
        assertThatExceptionOfType(OrderIdZeroException.class)
                .isThrownBy(() -> orderService.findOrdersById(orderId));
    }
}
