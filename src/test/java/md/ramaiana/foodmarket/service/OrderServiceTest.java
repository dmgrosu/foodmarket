package md.ramaiana.foodmarket.service;


import md.ramaiana.foodmarket.dao.ClientDao;
import md.ramaiana.foodmarket.dao.ProductDao;
import md.ramaiana.foodmarket.dao.OrderDao;
import md.ramaiana.foodmarket.model.Client;
import md.ramaiana.foodmarket.model.Product;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;


@ExtendWith({SpringExtension.class, MockitoExtension.class})
public class OrderServiceTest {
    @Mock
    OrderDao orderDaoMock;

    @Mock
    ProductDao productDaoMock;

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
    void test_addProductToOrder_orderSaved() throws Exception {
        //ARRANGE
        int orderId = 2;
        Product someProduct = Product.builder()
                .id(1)
                .price(15f)
                .weight(10f)
                .build();
        float quantity = 5f;
        Integer clientId = 3;
        when(productDaoMock.findByIdAndDeletedAtNull(eq(someProduct.getId())))
                .thenReturn(Optional.of(someProduct));
        when(clientDaoMock.findByIdAndDeletedAtNull(eq(clientId)))
                .thenReturn(Optional.of(Client.builder()
                        .id(3)
                        .build()));
        someExistingOrder(orderId);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        //ACT
        Order actualOrder = orderService.addProductToOrder(orderId, someProduct.getId(), quantity, clientId);
        //ASSERT
        verify(orderDaoMock).save(orderCaptor.capture());
    }

    @Test
    void test_addProductToOrder_responseProductValidationError_exceptionThrown() {
        //ARRANGE
        int productId = 1;
        //ACT & ASSERT
        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> orderService.addProductToOrder(2, productId, 15f, 5));
    }

    @Test
    void test_addProductToOrder_responseClientValidationError_exceptionThrown() {
        //ARRANGE
        int clientId = 1;
        when(productDaoMock.findByIdAndDeletedAtNull(6))
                .thenReturn(Optional.of(Product.builder().id(6).build()));
        //ACT & ASSERT
        assertThatExceptionOfType(ClientNotFoundException.class)
                .isThrownBy(() -> orderService.addProductToOrder(2, 6, 15f, clientId));
    }

    @Test
    void test_findOrderById_responseOk() throws Exception {
        //ARRANGE
        Integer orderId = 1;
        Order someOrder = Order.builder().build().withId(orderId);
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
        assertThatExceptionOfType(OrderNotFoundException.class)
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
        when(orderDaoMock.findAllByDeletedAtNullAndCreatedAtBetweenAndClient(pageable, from, to, clientId))
                .thenReturn(orderPage);
        when(clientDaoMock.findByIdAndDeletedAtNull(clientId))
                .thenReturn(Optional.of(Client.builder().id(clientId).build()));
        //ACT
        orderService.findOrdersByPeriod(from, to, clientId, page, perPage, direction, column);
        //ASSERT
        verify(orderDaoMock, times(1))
                .findAllByDeletedAtNullAndCreatedAtBetweenAndClient(pageable, from, to, clientId);
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
        when(clientDaoMock.findByIdAndDeletedAtNull(clientId))
                .thenReturn(null);
        //ACT & ASSERT
        assertThatExceptionOfType(ClientNotFoundException.class)
                .isThrownBy(() -> orderService.findOrdersByPeriod(from, to, clientId, page, perPage, direction, column));
    }

    @Test
    void test_updateProductQuantity() throws Exception {
        //ARRANGE
        int orderId = 1;
        int productId = 2;
        float newQuantity = 5.5f;
        Order someOrder = mock(Order.class);
        when(orderDaoMock.findByIdAndDeletedAtNull(orderId))
                .thenReturn(Optional.of(someOrder));
        Order updated = mock(Order.class);
        when(someOrder.updateQuantity(productId, newQuantity))
                .thenReturn(updated);
        //ACT
        orderService.updateProductQuantity(orderId, productId, newQuantity);
        //ASSERT
        verify(orderDaoMock).save(updated);
    }

    @Test
    void test_updateProductQuantity_validationNotPassed() {
        int orderId = 1;
        int goodId = 2;
        float newQuantity = 5.5f;
        when(orderDaoMock.findByIdAndDeletedAtNull(orderId))
                .thenReturn(Optional.empty());
        //ACT & ASSERT
        assertThatExceptionOfType(OrderNotFoundException.class)
                .isThrownBy(() -> orderService.updateProductQuantity(orderId, goodId, newQuantity));
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
        lenient().when(orderDaoMock.save(any())).thenReturn(order);
        lenient().when(orderDaoMock.existsByIdAndDeletedAtNull(orderId)).thenReturn(true);
        lenient().when(orderDaoMock.findByIdAndDeletedAtNull(orderId)).thenReturn(Optional.of(order));
        return order;
    }

}
