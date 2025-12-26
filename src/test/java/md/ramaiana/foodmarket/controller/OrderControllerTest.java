package md.ramaiana.foodmarket.controller;


import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderProduct;
import md.ramaiana.foodmarket.model.Product;
import md.ramaiana.foodmarket.proto.Common;
import md.ramaiana.foodmarket.proto.Orders;
import md.ramaiana.foodmarket.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderServiceMock;
    @MockitoBean
    private ProductService productServiceMock;

    @WithMockUser("spring")
    @Test
    void test_addGoodToNewOrder_quantityGreaterThanZero_responseOk() throws Exception {
        //ARRANGE
        float givenQuantity = 10f;
        int clientId = 55;
        givenNewOrder(givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct\\")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddProductToOrderRequest(0, 11, givenQuantity, clientId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(2));
    }

    @WithMockUser("spring")
    @Test
    void test_addGoodToNewOrder_quantityZero_responseBadRequest() throws Exception {
        //ARRANGE
        float givenQuantity = 0f;
        int clientId = 55;
        givenNewOrder(givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct\\")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddProductToOrderRequest(0, 11, givenQuantity, clientId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_addGoodToNewOrder_goodIdZero_responseBadRequest() throws Exception {
        //ARRANGE
        float givenQuantity = 10f;
        int clientId = 55;
        givenNewOrder(givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct\\")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddProductToOrderRequest(0, 0, givenQuantity, clientId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("GOOD_ID_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_addToExistingOrder_quantityGreaterThanZero_responseOk() throws Exception {
        //ARRANGE
        int orderId = 5;
        float givenQuantity = 10f;
        int clientId = 55;
        givenExistingOrder(orderId, givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct\\")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddProductToOrderRequest(orderId, 12, givenQuantity, clientId)))
                .andExpect(jsonPath("$.order.id").value(5))
                .andExpect(jsonPath("$.order.goods[0].goodId").value(11))
                .andExpect(jsonPath("$.order.goods[1].goodId").value(12));
    }

    @WithMockUser("spring")
    @Test
    void test_addToExistingOrder_quantityZero_responseBadRequest() throws Exception {
        //ARRANGE
        int orderId = 5;
        float givenQuantity = 0f;
        int clientId = 55;
        givenExistingOrder(orderId, givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct\\")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddProductToOrderRequest(orderId, 12, givenQuantity, clientId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_addToExistingOrder_goodIdZero_responseBadRequest() throws Exception {
        //ARRANGE
        int orderId = 5;
        float givenQuantity = 10f;
        int clientId = 55;
        givenExistingOrder(orderId, givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct\\")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddProductToOrderRequest(orderId, 0, givenQuantity, clientId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("GOOD_ID_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_addToOrder_serviceValidationNotPassed_clientNotFound_responseBadRequest() throws Exception {
        //ARRANGE
        when(orderServiceMock.addProductToOrder(1, 2, 3.5f, 4))
                .thenThrow(new ClientNotFoundException("Client with ID 4 not found"));

        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct\\")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddProductToOrderRequest(1, 2, 3.5f, 4)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("CLIENT_NOT_FOUND"));
    }

    @WithMockUser("spring")
    @Test
    void test_addToOrder_serviceValidationNotPassed_goodNotFound_responseBadRequest() throws Exception {
        //ARRANGE
        when(orderServiceMock.addProductToOrder(1, 2, 3.5f, 4))
                .thenThrow(new ProductNotFoundException("Good with ID 2 not found"));

        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct\\")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddProductToOrderRequest(1, 2, 3.5f, 4)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("GOOD_NOT_FOUND"));
    }

    @WithMockUser("spring")
    @Test
    void test_getOrderById_responseOk() throws Exception {
        //ARRANGE
        List<OrderProduct> someProducts = List.of(OrderProduct.builder()
                .id(1)
                .sum(200f)
                .weight(150f)
                .quantity(12f)
                .build());
        Order someOrder = Order.builder()
                .id(2)
                .client(AggregateReference.to(15))
                .products(someProducts)
                .totalSum(300f)
                .createdAt(OffsetDateTime.now())
                .build();
        when(orderServiceMock.findOrdersById(2))
                .thenReturn(someOrder);
        //ACT & ASSERT
        mockMvc.perform(get("/order/getById")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someGetByIdRequest(2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(2));
    }

    @WithMockUser("spring")
    @Test
    void test_getOrderById_serviceValidationNotPassed_OrderNotFound_responseBadRequest() throws Exception {
        //ARRANGE
        when(orderServiceMock.findOrdersById(1))
                .thenThrow(new OrderNotFoundException("Order with ID 1 not found"));
        //ACT & ASSERT
        mockMvc.perform(get("/order/getById")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someGetByIdRequest(1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("ORDER_NOT_FOUND"));
    }

    @WithMockUser("spring")
    @Test
    void test_getOrderById_ValidationNotPassed_OrderIdIsZero_responseBadRequest() throws Exception {
        //ARRANGE & ACT & ASSERT
        mockMvc.perform(get("/order/getById")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someGetByIdRequest(0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("ORDER_ID_IS_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_deleteOrderById_responseOk() throws Exception {
        //ARRANGE & ACT & ASSERT
        mockMvc.perform(post("/order/deleteById")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someDeleteOrderRequest(1)))
                .andExpect(status().isOk());
    }

    @WithMockUser("spring")
    @Test
    void test_getOrdersByPeriod_responseOk() throws Exception {
        //ARRANGE
        long from = 1615986580054L;
        long to = 1615986589387L;
        OffsetDateTime dateFrom = OffsetDateTime.ofInstant(Instant.ofEpochMilli(from), ZoneId.of("UTC"));
        OffsetDateTime dateTo = OffsetDateTime.ofInstant(Instant.ofEpochMilli(to), ZoneId.of("UTC"));
        int clientId = 3;
        String direction = "DESC";
        String column = "id";
        Pageable pageable = PageRequest.of(1, 1, Sort.Direction.valueOf(direction), column);
        List<OrderProduct> orderProducts = List.of(givenOrderGoodForExistingOrder(givenProduct("someName", 15f, 10f), 15, 11));
        List<Order> orders = new ArrayList<>();
        orders.add(Order.builder()
                .id(1)
                .createdAt(dateFrom.plusHours(2))
                .client(AggregateReference.to(clientId))
                .totalSum(300f)
                .products(orderProducts)
                .build());
        Page<Order> orderPage = new PageImpl<>(orders, pageable, pageable.getOffset());
        Common.Pagination pagination = Common.Pagination.newBuilder()
                .setPageNo(1)
                .setPerPage(1)
                .build();
        Common.Sorting sorting = Common.Sorting.newBuilder()
                .setDirection(Common.Sorting.Direction.valueOf(direction))
                .setColumnName(column)
                .build();
        when(orderServiceMock.findOrdersByPeriod(eq(dateFrom), eq(dateTo), eq(clientId), eq(1), eq(1), eq(direction), eq(column)))
                .thenReturn(orderPage);
        //ACT & ASSERT
        mockMvc.perform(post("/order/getOrdersByPeriod")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someGetByPeriodRequest(from, to, clientId, pagination, sorting)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].id").value(1));
    }

    @WithMockUser("spring")
    @Test
    void test_getOrdersByPeriod_pageIsLessThanZero_responseBadRequest() throws Exception {
        //ARRANGE
        long from = 1615986580054L;
        long to = 1615986589387L;
        String direction = "DESC";
        String column = "id";
        Common.Pagination pagination = Common.Pagination.newBuilder()
                .setPageNo(-1)
                .setPerPage(1)
                .build();
        Common.Sorting sorting = Common.Sorting.newBuilder()
                .setDirection(Common.Sorting.Direction.valueOf(direction))
                .setColumnName(column)
                .build();
        //ACT & ASSERT
        mockMvc.perform(post("/order/getOrdersByPeriod")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someGetByPeriodRequest(from, to, 15, pagination, sorting)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("PAGE_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_getOrdersByPeriod_perPageIsLessThanZero_responseBadRequest() throws Exception {
        //ARRANGE
        long from = 1615986580054L;
        long to = 1615986589387L;
        String direction = "DESC";
        String column = "id";
        Common.Pagination pagination = Common.Pagination.newBuilder()
                .setPageNo(1)
                .setPerPage(-1)
                .build();
        Common.Sorting sorting = Common.Sorting.newBuilder()
                .setDirection(Common.Sorting.Direction.valueOf(direction))
                .setColumnName(column)
                .build();
        //ACT & ASSERT
        mockMvc.perform(post("/order/getOrdersByPeriod")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someGetByPeriodRequest(from, to, 15, pagination, sorting)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("PAGE_SIZE_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_getOrdersByPeriod_perPageIsLessThanZeroAndPageIsLessThanZero_responseBadRequest() throws Exception {
        //ARRANGE
        long from = 1615986580054L;
        long to = 1615986589387L;
        String direction = "DESC";
        String column = "id";
        Common.Pagination pagination = Common.Pagination.newBuilder()
                .setPageNo(-1)
                .setPerPage(-1)
                .build();
        Common.Sorting sorting = Common.Sorting.newBuilder()
                .setDirection(Common.Sorting.Direction.valueOf(direction))
                .setColumnName(column)
                .build();
        //ACT & ASSERT
        mockMvc.perform(post("/order/getOrdersByPeriod")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someGetByPeriodRequest(from, to, 15, pagination, sorting)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("PAGE_IS_LESS_OR_EQUAL_TO_ZERO"))
                .andExpect(jsonPath("$.errors[1].code").value("PAGE_SIZE_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_updateOrder_responseOk() throws Exception {
        //ARRANGE & ACT & ASSERT
        mockMvc.perform(post("/order/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someUpdateOrderRequest(1, 2, 5.5f)))
                .andExpect(status().isOk());
    }

    @WithMockUser("spring")
    @Test
    void test_updateOrder_quantityIsLessThanZero_responseBadRequest() throws Exception {
        //ARRANGE & ACT & ASSERT
        mockMvc.perform(post("/order/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someUpdateOrderRequest(1, 2, -5.5f)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @Test
    @WithMockUser("someUser")
    void test_placeOrder_responseOk() throws Exception {
        mockMvc.perform(post("/order/placeOrder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(somePlaceOrderRequest(1)))
                .andExpect(status().isOk());

        verify(orderServiceMock, times(1)).placeOrder(eq(1));
    }

    @Test
    @WithMockUser("someUser")
    void test_placeOrder_noOrderId_badRequest() throws Exception {
        mockMvc.perform(post("/order/placeOrder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(somePlaceOrderRequest(0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("ORDER_NOT_FOUND"));

        verify(orderServiceMock, never()).placeOrder(eq(1));
    }

    private void givenNewOrder(float goodQuantity, int clientId) throws Exception {
        Product someProduct = givenProduct("someName", 10f, 1.5f);
        OrderProduct givenOrderProduct = givenOrderGoodForNewOrder(someProduct, goodQuantity);
        List<OrderProduct> givenGoods = List.of(givenOrderProduct);
        Order givenOrder = Order.builder()
                .id(2)
                .products(givenGoods)
                .client(AggregateReference.to(clientId))
                .totalSum(400f)
                .createdAt(OffsetDateTime.now())
                .build();
        when(orderServiceMock.addProductToOrder(eq(0), eq(someProduct.getId()), eq(goodQuantity), eq(clientId)))
                .thenReturn(givenOrder);
    }

    private void givenExistingOrder(int orderId, float givenQuantity, int clientId) throws Exception {
        Product someProduct1 = givenProduct("someName", 10f, 1.5f);
        Product someProduct2 = givenOtherGood("someName2", 10f, 1.5f);
        List<OrderProduct> givenProducts = List.of(
                givenOrderGoodForExistingOrder(someProduct1, givenQuantity, 11),
                givenOrderGoodForExistingOrder(someProduct2, givenQuantity, 12)
        );
        Order givenOrder = Order.builder()
                .id(orderId)
                .products(givenProducts)
                .client(AggregateReference.to(clientId))
                .totalSum(400f)
                .createdAt(OffsetDateTime.now())
                .build();
        when(orderServiceMock.addProductToOrder(eq(orderId), eq(someProduct2.getId()), eq(givenQuantity), eq(clientId)))
                .thenReturn(givenOrder);
    }

    private OrderProduct givenOrderGoodForNewOrder(Product product, float quantity) {
        return OrderProduct.builder()
                .id(10)
                .product(AggregateReference.to(product.getId()))
                .quantity(quantity)
                .sum(product.getPrice() * quantity)
                .weight(product.getWeight() * quantity)
                .build();
    }


    private OrderProduct givenOrderGoodForExistingOrder(Product product, float quantity, int orderGoodId) {
        return OrderProduct.builder()
                .id(orderGoodId)
                .product(AggregateReference.to(product.getId()))
                .quantity(quantity)
                .sum(product.getPrice() * quantity)
                .weight(product.getWeight() * quantity)
                .build();
    }

    private Product givenProduct(String name, Float price, Float weight) {
        return Product.builder()
                .id(11)
                .name(name)
                .price(price)
                .groupId(0)
                .brandId(0)
                .unit("unit")
                .inPackage(15f)
                .barCode("barCode")
                .weight(weight)
                .build();
    }

    private Product givenOtherGood(String name, Float price, Float weight) {
        return Product.builder()
                .id(12)
                .name(name)
                .price(price)
                .groupId(0)
                .brandId(0)
                .unit("unit")
                .inPackage(15f)
                .barCode("barCode")
                .weight(weight)
                .build();
    }

    private String someAddProductToOrderRequest(int orderId, int goodId, Float quantity, Integer clientId) throws InvalidProtocolBufferException {
        Orders.AddProductToOrderRequest protoRequest = Orders.AddProductToOrderRequest.newBuilder()
                .setOrderId(orderId)
                .setProductId(goodId)
                .setQuantity(quantity)
                .setClientId(clientId)
                .build();
        return JsonFormat.printer().print(protoRequest);

    }

    private String someDeleteOrderRequest(int orderId) throws InvalidProtocolBufferException {
        Orders.DeleteOrderRequest protoRequest = Orders.DeleteOrderRequest.newBuilder()
                .setOrderId(orderId)
                .build();
        return JsonFormat.printer().print(protoRequest);
    }

    private String someGetByPeriodRequest(long from, long to, int clientId, Common.Pagination pagination, Common.Sorting sorting) throws InvalidProtocolBufferException {
        Orders.OrderListRequest protoRequest = Orders.OrderListRequest.newBuilder()
                .setDateFrom(from)
                .setDateTo(to)
                .setClientId(clientId)
                .setPagination(pagination)
                .setSorting(sorting)
                .build();
        return JsonFormat.printer().print(protoRequest);
    }

    private String someUpdateOrderRequest(int orderId, int productId, float newQuantity) throws InvalidProtocolBufferException {
        Orders.UpdateOrderRequest protoRequest = Orders.UpdateOrderRequest.newBuilder()
                .setOrderId(orderId)
                .setProductId(productId)
                .setNewQuantity(newQuantity)
                .build();
        return JsonFormat.printer().print(protoRequest);
    }

    private String somePlaceOrderRequest(int orderId) throws InvalidProtocolBufferException {
        Orders.PlaceOrderRequest request = Orders.PlaceOrderRequest.newBuilder()
                .setOrderId(orderId)
                .build();
        return JsonFormat.printer().print(request);
    }

    private String someGetByIdRequest(int orderId) throws InvalidProtocolBufferException {
        Orders.GetOrderByIdRequest protoRequest = Orders.GetOrderByIdRequest.newBuilder()
                .setOrderId(orderId)
                .build();
        return JsonFormat.printer().print(protoRequest);
    }
}
