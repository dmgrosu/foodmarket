package md.ramaiana.foodmarket.controller;


import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderGood;
import md.ramaiana.foodmarket.proto.Common;
import md.ramaiana.foodmarket.proto.Orders;
import md.ramaiana.foodmarket.service.ClientNotFoundException;
import md.ramaiana.foodmarket.service.GoodNotFoundException;
import md.ramaiana.foodmarket.service.OrderNotFoundException;
import md.ramaiana.foodmarket.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderServiceMock;

    @WithMockUser("spring")
    @Test
    void test_addGoodToNewOrder_quantityGreaterThanZero_responseOk() throws Exception {
        //ARRANGE
        float givenQuantity = 10f;
        int clientId = 55;
        givenNewOrder(givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/addGood")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(0, 11, givenQuantity, clientId)))
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
        mockMvc.perform(post("/order/addGood")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(0, 11, givenQuantity, clientId)))
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
        mockMvc.perform(post("/order/addGood")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(0, 0, givenQuantity, clientId)))
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
        mockMvc.perform(post("/order/addGood")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(orderId, 12, givenQuantity, clientId)))
                .andExpect(jsonPath("$.order.id").value(5))
                .andExpect(jsonPath("$.order.goods[0].id").value(11))
                .andExpect(jsonPath("$.order.goods[1].id").value(12));
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
        mockMvc.perform(post("/order/addGood")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(orderId, 12, givenQuantity, clientId)))
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
        mockMvc.perform(post("/order/addGood")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(orderId, 0, givenQuantity, clientId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("GOOD_ID_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_addToOrder_serviceValidationNotPassed_clientNotFound_responseBadRequest() throws Exception {
        //ARRANGE
        when(orderServiceMock.addGoodToOrder(1, 2, 3.5f, 4))
                .thenThrow(new ClientNotFoundException("Client with ID 4 not found"));

        //ACT & ASSERT
        mockMvc.perform(post("/order/addGood")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(1, 2, 3.5f, 4)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("CLIENT_NOT_FOUND"));
    }

    @WithMockUser("spring")
    @Test
    void test_addToOrder_serviceValidationNotPassed_goodNotFound_responseBadRequest() throws Exception {
        //ARRANGE
        when(orderServiceMock.addGoodToOrder(1, 2, 3.5f, 4))
                .thenThrow(new GoodNotFoundException("Good with ID 2 not found"));

        //ACT & ASSERT
        mockMvc.perform(post("/order/addGood")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(1, 2, 3.5f, 4)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("GOOD_NOT_FOUND"));
    }

    @WithMockUser("spring")
    @Test
    void test_getOrderById_responseOk() throws Exception {
        //ARRANGE
        OrderGood someOrderGood = OrderGood.builder().id(1).orderId(2).sum(200f).weight(150f).build();
        Set<OrderGood> someGoods = new HashSet<>();
        someGoods.add(someOrderGood);
        Order someOrder = Order.builder().id(2).clientId(15).goods(someGoods).createdAt(OffsetDateTime.now()).build();
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
        int clintId = 3;
        String direction = "DESC";
        String column = "id";
        Pageable pageable = PageRequest.of(1, 1, Sort.Direction.valueOf(direction), column);
        Set<OrderGood> orderGoods = new HashSet<>();
        orderGoods.add(givenOrderGoodForExistingOrder(1, givenGood("someName", 15f, 10f), 15, 11));
        List<Order> orders = new ArrayList<>();
        orders.add(Order.builder().id(1).createdAt(dateFrom.plusHours(2)).clientId(clintId).goods(orderGoods).build());
        Page<Order> orderPage = new PageImpl<>(orders, pageable, pageable.getOffset());
        Common.Pagination pagination = Common.Pagination.newBuilder()
                .setPageNo(1)
                .setPerPage(1)
                .build();
        Common.Sorting sorting = Common.Sorting.newBuilder()
                .setDirection(Common.Sorting.Direction.valueOf(direction))
                .setColumnName(column)
                .build();
        when(orderServiceMock.findOrdersByPeriod(eq(dateFrom), eq(dateTo), eq(clintId), eq(1), eq(1), eq(direction), eq(column)))
                .thenReturn(orderPage);
        //ACT & ASSERT
        mockMvc.perform(post("/order/getOrdersByPeriod")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someGetByPeriodRequest(from, to, clintId, pagination, sorting)))
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


    private void givenNewOrder(float goodQuantity, int clientId) throws Exception {
        Good someGood = givenGood("someName", 10f, 1.5f);
        OrderGood givenOrderGood = givenOrderGoodForNewOrder(someGood, goodQuantity);
        Set<OrderGood> givenGoods = new HashSet<>();
        givenGoods.add(givenOrderGood);
        Order givenOrder = Order.builder()
                .id(2)
                .goods(givenGoods)
                .clientId(clientId)
                .createdAt(OffsetDateTime.now())
                .build();
        when(orderServiceMock.addGoodToOrder(eq(0), eq(someGood.getId()), eq(goodQuantity), eq(clientId)))
                .thenReturn(givenOrder);
    }

    private void givenExistingOrder(int orderId, float givenQuantity, int clientId) throws Exception {
        Good someGood1 = givenGood("someName", 10f, 1.5f);
        Good someGood2 = givenOtherGood("someName2", 10f, 1.5f);
        OrderGood givenOrderGood1 = givenOrderGoodForExistingOrder(orderId, someGood1, givenQuantity, 11);
        OrderGood givenOrderGood2 = givenOrderGoodForExistingOrder(orderId, someGood2, givenQuantity, 12);
        Set<OrderGood> givenGoods = new HashSet<>();
        givenGoods.add(givenOrderGood1);
        givenGoods.add(givenOrderGood2);
        Order givenOrder = Order.builder()
                .id(orderId)
                .goods(givenGoods)
                .clientId(clientId)
                .createdAt(OffsetDateTime.now())
                .build();
        when(orderServiceMock.addGoodToOrder(eq(orderId), eq(someGood2.getId()), eq(givenQuantity), eq(clientId)))
                .thenReturn(givenOrder);
    }

    private OrderGood givenOrderGoodForNewOrder(Good good, float quantity) {
        return OrderGood.builder()
                .id(10)
                .goodId(good.getId())
                .quantity(quantity)
                .sum(good.getPrice() * quantity)
                .weight(good.getWeight() * quantity)
                .build();
    }


    private OrderGood givenOrderGoodForExistingOrder(int orderId, Good good, float quantity, int orderGoodId) {
        return OrderGood.builder()
                .id(orderGoodId)
                .orderId(orderId)
                .goodId(good.getId())
                .quantity(quantity)
                .sum(good.getPrice() * quantity)
                .weight(good.getWeight() * quantity)
                .build();
    }

    private Good givenGood(String name, Float price, Float weight) {
        return Good.builder()
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

    private Good givenOtherGood(String name, Float price, Float weight) {
        return Good.builder()
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

    private String someAddGoodToOrderRequest(int orderId, int goodId, Float quantity, Integer clientId) throws InvalidProtocolBufferException {
        Orders.AddGoodToOrderRequest protoRequest = Orders.AddGoodToOrderRequest.newBuilder()
                .setOrderId(orderId)
                .setGoodId(goodId)
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

    private String someUpdateOrderRequest(int orderId, int goodId, float newQuantity) throws InvalidProtocolBufferException {
        Orders.UpdateOrderRequest protoRequest = Orders.UpdateOrderRequest.newBuilder()
                .setOrderId(orderId)
                .setGoodId(goodId)
                .setNewQuantity(newQuantity)
                .build();
        return JsonFormat.printer().print(protoRequest);
    }

    private String someGetByIdRequest(int orderId) throws InvalidProtocolBufferException {
        Orders.GetOrderByIdRequest protoRequest = Orders.GetOrderByIdRequest.newBuilder()
                .setOrderId(orderId)
                .build();
        return JsonFormat.printer().print(protoRequest);
    }
}
