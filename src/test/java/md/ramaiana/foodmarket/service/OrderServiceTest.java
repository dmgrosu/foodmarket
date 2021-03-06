package md.ramaiana.foodmarket.service;


import md.ramaiana.foodmarket.dao.ClientDao;
import md.ramaiana.foodmarket.dao.GoodDao;
import md.ramaiana.foodmarket.dao.OrderDao;
import md.ramaiana.foodmarket.dao.OrderGoodDao;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;


@ExtendWith({SpringExtension.class})
public class OrderServiceTest {
    @Mock
    OrderDao orderDaoMock;

    @Mock
    GoodDao goodDaoMock;

    @Mock
    OrderGoodDao orderGoodDaoMock;

    @Mock
    ClientDao clientDaoMock;

    @InjectMocks
    OrderService orderService;


//    @Test
//    void test_deleteOrder() {
//        //ARRANGE
//        Integer orderId = 1;
//        //ACT
//        orderService.deleteOrder(orderId);
//        //ASSERT
//        verify(orderDaoMock, times(1))
//                .deleteOrderById(orderId);
//    }

    @Test
    void test_addGoodToOrder_orderSaved() throws Exception {
        //ARRANGE
        int orderId = 2;
        Good someGood = Good.builder()
                .id(1)
                .price(15f)
                .weight(10f)
                .build();
        float quantity = 5f;
        Integer clientId = 3;
        when(goodDaoMock.getByIdAndDeletedAtNull(eq(someGood.getId())))
                .thenReturn(someGood);
        when(clientDaoMock.getByIdAndDeletedAtNull(eq(clientId)))
                .thenReturn(Client.builder()
                        .id(3)
                        .build());
        someExistingOrder(orderId);
        when(orderGoodDaoMock.existsByOrderIdAndGoodId(eq(orderId), eq(someGood.getId()))).thenReturn(false);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        //ACT
        Order actualOrder = orderService.addGoodToOrder(orderId, someGood.getId(), quantity, clientId);
        //ASSERT
        verify(orderDaoMock, times(1)).save(orderCaptor.capture());
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
        when(orderDaoMock.findByIdAndDeletedAtNull(eq(orderId)))
                .thenReturn(Optional.of(someOrder));
        //ACT
        orderService.findOrdersById(someOrder.getId());
        //ASSERT
        verify(orderDaoMock, times(1))
                .findByIdAndDeletedAtNull(orderId);
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

    @Test
    void test_deleteOrderById() {
        //ARRANGE
        Integer orderId = 1;
        //ACT
        orderService.deleteOrderById(orderId);
        //ASSERT
        verify(orderDaoMock, times(1))
                .setOrderToDeletedState(orderId);
    }

    @Test
    void test_findOrdersByPeriod_returnsPage() throws Exception {
        //ARRANGE
        OffsetDateTime from = OffsetDateTime.now();
        OffsetDateTime to = OffsetDateTime.now();
        int clientId = 5;
        int page = 7;
        int perPage = 2;
        String direction = "DESC";
        String column = "id";
        Pageable pageable = PageRequest.of(page, perPage, Sort.Direction.valueOf(direction), column);
        List<Order> orders = new ArrayList<>();
        orders.add(Order.builder().id(1).build());
        Page<Order> orderPage = new PageImpl<>(orders, pageable, pageable.getOffset());
        when(orderDaoMock.findAllByDeletedAtNullAndCreatedAtBetweenAndClientId(pageable, from, to, clientId))
                .thenReturn(orderPage);
        when(clientDaoMock.getByIdAndDeletedAtNull(clientId))
                .thenReturn(Client.builder().id(clientId).build());
        //ACT
        orderService.findOrdersByPeriod(from, to, clientId, page, perPage, direction, column);
        //ASSERT
        verify(orderDaoMock, times(1))
                .findAllByDeletedAtNullAndCreatedAtBetweenAndClientId(pageable, from, to, clientId);
    }

    @Test
    void test_findOrdersByPeriod_responseClientValidationError_exceptionThrown() {
        //ARRANGE
        OffsetDateTime from = OffsetDateTime.now();
        OffsetDateTime to = OffsetDateTime.now();
        int clientId = 5;
        int page = 7;
        int perPage = 2;
        String direction = "DESC";
        String column = "id";
        when(clientDaoMock.getByIdAndDeletedAtNull(clientId))
                .thenReturn(null);
        //ACT & ASSERT
        assertThatExceptionOfType(ClientNotFoundException.class)
                .isThrownBy(() -> orderService.findOrdersByPeriod(from, to, clientId, page, perPage, direction, column));
    }

    @Test
    void test_updateOrder() throws GoodNotFoundException {
        //ARRANGE
        int orderId = 1;
        int goodId = 2;
        float newQuantity = 5.5f;
        Good someGood = Good.builder().id(goodId).build();
        when(goodDaoMock.getByIdAndDeletedAtNull(goodId))
                .thenReturn(someGood);
        //ACT
        orderService.updateOrder(orderId, goodId, newQuantity);
        //ASSERT
        verify(orderGoodDaoMock, times(1))
                .updateOrderGoodQuantity(orderId, goodId, newQuantity);
    }

    @Test
    void test_updateOrder_validationNotPassed() {
        int orderId = 1;
        int goodId = 2;
        float newQuantity = 5.5f;
        when(goodDaoMock.getByIdAndDeletedAtNull(goodId))
                .thenReturn(null);
        //ACT & ASSERT
        assertThatExceptionOfType(GoodNotFoundException.class)
                .isThrownBy(() -> orderService.updateOrder(orderId, goodId, newQuantity));
    }

    @Test
    void test_placeOrder() throws Exception {
        // ARRANGE
        someExistingOrder(12);
        // ACT
        orderService.placeOrder(12);
        // ASSERT
        verify(orderDaoMock, times(1)).updateOrderState(eq(OrderState.PLACED), eq(12));
    }

    @Test
    void test_placeOrder_orderNotFound_exceptionThrown() {
        // ARRANGE
        someExistingOrder(1);
        // ACT & ASSERT
        assertThatExceptionOfType(OrderNotFoundException.class)
                .isThrownBy(() -> orderService.placeOrder(2));
    }

    private Order someExistingOrder(int orderId) {
        Order order = Order.builder()
                .id(orderId)
                .build();
        when(orderDaoMock.save(any())).thenReturn(order);
        when(orderDaoMock.existsByIdAndDeletedAtNull(orderId)).thenReturn(true);
        when(orderDaoMock.findByIdAndDeletedAtNull(orderId)).thenReturn(Optional.of(order));
        return order;
    }

}
