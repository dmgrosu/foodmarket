package md.ramaiana.foodmarket.controller;


import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderGood;
import md.ramaiana.foodmarket.proto.Orders;
import md.ramaiana.foodmarket.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        mockMvc.perform(post("/order/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(0, 11, givenQuantity, clientId)))
                .andDo(MockMvcResultHandlers.print())
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
        mockMvc.perform(post("/order/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(0, 11, givenQuantity, clientId)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_addGoodToNewOrder_goodIdZero_responseBadRequest() throws Exception {
        //ARRANGE
        float givenQuantity = 10f;
        int clientId = 55;
        givenNewOrder(givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(0, 0, givenQuantity, clientId)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GOOD_ID_IS_LESS_OR_EQUAL_TO_ZERO"));
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
        mockMvc.perform(post("/order/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(orderId, 12, givenQuantity, clientId)))
                .andDo(MockMvcResultHandlers.print())
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
        mockMvc.perform(post("/order/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(orderId, 12, givenQuantity, clientId)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO"));
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
        mockMvc.perform(post("/order/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(orderId, 0, givenQuantity, clientId)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GOOD_ID_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_addToOrder_serviceValidationNotPassed_clientNotFound_responseOk() throws Exception {
        //ARRANGE
        when(orderServiceMock.addGoodToOrder(1, 2, 3.5f, 4))
                .thenThrow(new ClientNotFoundException("Client with ID 4 not found"));

        //ACT & ASSERT
        mockMvc.perform(post("/order/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(1, 2, 3.5f, 4)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CLIENT_NOT_FOUND"));
    }

    @WithMockUser("spring")
    @Test
    void test_addToOrder_serviceValidationNotPassed_goodNotFound_responseOk() throws Exception {
        //ARRANGE
        when(orderServiceMock.addGoodToOrder(1, 2, 3.5f, 4))
                .thenThrow(new GoodNotFoundException("Good with ID 2 not found"));

        //ACT & ASSERT
        mockMvc.perform(post("/order/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(1, 2, 3.5f, 4)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GOOD_NOT_FOUND"));
    }

    @WithMockUser("spring")
    @Test
    void test_getOrderById_responseOk() throws Exception {
        //ARRANGE
        OrderGood someOrderGood = OrderGood.builder().id(1).orderId(2).sum(200f).weight(150f).build();
        List<OrderGood> someGoods = new ArrayList<>();
        someGoods.add(someOrderGood);
        Order someOrder = Order.builder().id(2).clientId(15).goods(someGoods).createdAt(OffsetDateTime.now()).build();
        when(orderServiceMock.findOrdersById(2))
                .thenReturn(someOrder);
        //ACT & ASSERT
        mockMvc.perform(get("/order/getById")
                .param("id", "2"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(2));
    }

    @WithMockUser("spring")
    @Test
    void test_getOrderById_serviceValidationNotPassed_OrderNotFound_responseOk() throws Exception {
        //ARRANGE
        when(orderServiceMock.findOrdersById(1))
                .thenThrow(new OrderNotFoundException("Order with ID 1 not found"));
        //ACT & ASSERT
        mockMvc.perform(get("/order/getById")
                .param("id", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @WithMockUser("spring")
    @Test
    void test_getOrderById_ValidationNotPassed_OrderIdIsZero_responseBadRequest() throws Exception {
        //ARRANGE & ACT & ASSERT
        mockMvc.perform(get("/order/getById")
                .param("id", "0"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ORDER_ID_IS_ZERO"));
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


    private void givenNewOrder(float goodQuantity, int clientId) throws GoodNotFoundException, ClientNotFoundException {
        Good someGood = givenGood("someName", 10f, 1.5f);
        OrderGood givenOrderGood = givenOrderGoodForNewOrder(someGood, goodQuantity);
        List<OrderGood> givenGoods = Collections.singletonList(givenOrderGood);
        Order givenOrder = Order.builder()
                .id(2)
                .goods(givenGoods)
                .clientId(clientId)
                .createdAt(OffsetDateTime.now())
                .build();
        when(orderServiceMock.addGoodToOrder(eq(0), eq(someGood.getId()), eq(goodQuantity), eq(clientId)))
                .thenReturn(givenOrder);
    }

    private void givenExistingOrder(int orderId, float givenQuantity, int clientId) throws GoodNotFoundException, ClientNotFoundException {
        Good someGood1 = givenGood("someName", 10f, 1.5f);
        Good someGood2 = givenOtherGood("someName2", 10f, 1.5f);
        OrderGood givenOrderGood1 = givenOrderGoodForExistingOrder(orderId, someGood1, givenQuantity, 11);
        OrderGood givenOrderGood2 = givenOrderGoodForExistingOrder(orderId, someGood2, givenQuantity, 12);
        List<OrderGood> givenGoods = new ArrayList<>();
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
}