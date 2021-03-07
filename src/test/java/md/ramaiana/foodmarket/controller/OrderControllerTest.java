package md.ramaiana.foodmarket.controller;


import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import md.ramaiana.foodmarket.model.Good;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderGood;
import md.ramaiana.foodmarket.proto.Goods;
import md.ramaiana.foodmarket.proto.Orders;
import md.ramaiana.foodmarket.service.OrderService;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
        mockMvc.perform(post("/order/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(someAddGoodToOrderRequest(0, 11, givenQuantity, clientId)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(2));
    }

    private void givenNewOrder(float goodQuantity, int clientId) {
        Good someGood = givenGood("someName", 10f, 1.5f);
        OrderGood givenOrderGood1 = givenOrderGoodForNewOrder(someGood, goodQuantity);
        List<OrderGood> givenGoods = Collections.singletonList(givenOrderGood1);
        Order givenOrder = Order.builder()
                .id(2)
                .goods(givenGoods)
                .clientId(clientId)
                .createdAt(OffsetDateTime.now()).build();
        when(orderServiceMock.addGoodToOrder(eq(0), eq(someGood.getId()), eq(goodQuantity), eq(clientId)))
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

    private String someAddGoodToOrderRequest(int orderId, int goodId, Float quantity, Integer clientId) throws InvalidProtocolBufferException {
        Goods.Good protoGood = Goods.Good.newBuilder()
                .setId(goodId)
                .setName("someName")
                .setPrice(10f)
                .setUnit("unit")
                .setPackage(15f)
                .setBarCode("barCode")
                .setWeight(1.5f)
                .build();
        Orders.AddGoodToOrderRequest protoRequest = Orders.AddGoodToOrderRequest.newBuilder()
                .setOrderId(orderId)
                .setGoodId(goodId)
                .setQuantity(quantity)
                .setClientId(clientId)
                .build();
        return JsonFormat.printer().print(protoRequest);
    }
}
