package md.ramaiana.foodmarket.service;


import md.ramaiana.foodmarket.dao.OrderDao;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.*;


@ExtendWith({SpringExtension.class})
public class OrderServiceTest {
    @Mock
    OrderDao orderDaoMock;

    @InjectMocks
    OrderService orderService;

    @Test
    void test_findOrderById() {
        //ARRANGE
        Integer orderId = 1;
        Order someOrder = Order.builder()
                .id(orderId)
                .build();
        when(orderDaoMock.getByIdAndDeletedAtNull(eq(orderId))).thenReturn(someOrder);
        //ACT
        orderService.findOrdersById(someOrder.getId());
        //ASSERT
        verify(orderDaoMock, times(1))
                .getByIdAndDeletedAtNull(orderId);
    }

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
    void test_addGoodToOrder() throws GoodNotFoundException, ClientNotFoundException {
        //ARRANGE
        Integer orderId = 2;
        Good someGood = Good.builder().id(1).price(15f).build();
        Float quantity = 5f;
        Integer clientId = 3;
        Float sum = someGood.getPrice() * quantity;
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
}
