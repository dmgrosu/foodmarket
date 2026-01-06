package md.ramaiana.foodmarket.controller;


import lombok.NonNull;
import md.ramaiana.foodmarket.controller.dto.common.PaginationDto;
import md.ramaiana.foodmarket.controller.dto.common.SortingDto;
import md.ramaiana.foodmarket.controller.dto.orders.AddProductToOrderRequestDto;
import md.ramaiana.foodmarket.controller.dto.orders.OrderListRequestDto;
import md.ramaiana.foodmarket.controller.dto.orders.UpdateOrderRequestDto;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderProduct;
import md.ramaiana.foodmarket.model.Product;
import md.ramaiana.foodmarket.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.*;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private ProductService productService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser("spring")
    void test_addProductToNewOrder_quantityGreaterThanZero_responseOk() throws Exception {
        //ARRANGE
        float givenQuantity = 10f;
        int clientId = 55;
        givenNewOrder(givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someAddProductToOrderRequest(0, 11, givenQuantity, clientId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @WithMockUser("spring")
    @Test
    void test_addProductToNewOrder_quantityZero_responseBadRequest() throws Exception {
        //ARRANGE
        float givenQuantity = 0f;
        int clientId = 55;
        givenNewOrder(givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someAddProductToOrderRequest(0, 11, givenQuantity, clientId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_addProductToNewOrder_productIdZero_responseBadRequest() throws Exception {
        //ARRANGE
        float givenQuantity = 10f;
        int clientId = 55;
        givenNewOrder(givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someAddProductToOrderRequest(0, 0, givenQuantity, clientId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("GOOD_ID_IS_LESS_OR_EQUAL_TO_ZERO"));
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
        mockMvc.perform(post("/order/addProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someAddProductToOrderRequest(orderId, 12, givenQuantity, clientId)))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.items[0].productId").value(11))
                .andExpect(jsonPath("$.items[1].productId").value(12));
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
        mockMvc.perform(post("/order/addProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someAddProductToOrderRequest(orderId, 12, givenQuantity, clientId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_addToExistingOrder_productIdZero_responseBadRequest() throws Exception {
        //ARRANGE
        int orderId = 5;
        float givenQuantity = 10f;
        int clientId = 55;
        givenExistingOrder(orderId, givenQuantity, clientId);
        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someAddProductToOrderRequest(orderId, 0, givenQuantity, clientId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("GOOD_ID_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @WithMockUser("spring")
    @Test
    void test_addToOrder_serviceValidationNotPassed_clientNotFound_responseBadRequest() throws Exception {
        //ARRANGE
        when(orderService.addProductToOrder(1, 2, 3.5f, 4))
                .thenThrow(new ClientNotFoundException("Client with ID 4 not found"));

        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someAddProductToOrderRequest(1, 2, 3.5f, 4)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("CLIENT_NOT_FOUND"));
    }

    @WithMockUser("spring")
    @Test
    void test_addToOrder_serviceValidationNotPassed_productNotFound_responseBadRequest() throws Exception {
        //ARRANGE
        when(orderService.addProductToOrder(1, 2, 3.5f, 4))
                .thenThrow(new ProductNotFoundException("Product with ID 2 not found"));

        //ACT & ASSERT
        mockMvc.perform(post("/order/addProduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someAddProductToOrderRequest(1, 2, 3.5f, 4)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("GOOD_NOT_FOUND"));
    }

    @Test
    @WithMockUser("spring")
    void test_getOrderById_responseOk() throws Exception {
        //ARRANGE
        List<OrderProduct> someProducts = List.of(OrderProduct.builder()
                .id(1)
                .product(AggregateReference.to(11))
                .price(10f)
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
        when(orderService.findOrdersById(2))
                .thenReturn(someOrder);
        //ACT & ASSERT
        mockMvc.perform(get("/order/getById/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @WithMockUser("spring")
    void test_getOrderById_serviceValidationNotPassed_OrderNotFound_responseBadRequest() throws Exception {
        when(orderService.findOrdersById(1))
                .thenThrow(new OrderNotFoundException("Order with ID 1 not found"));

        mockMvc.perform(get("/order/getById/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("ORDER_NOT_FOUND"));
    }

    @Test
    @WithMockUser("spring")
    void test_getOrderById_ValidationNotPassed_OrderIdIsZero_responseBadRequest() throws Exception {
        mockMvc.perform(get("/order/getById/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("ORDER_ID_IS_ZERO"));
    }

    @Test
    @WithMockUser("spring")
    void test_deleteOrderById_responseOk() throws Exception {
        //ARRANGE & ACT & ASSERT
        mockMvc.perform(delete("/order/deleteById/1"))
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
        String column = "id";
        Pageable pageable = PageRequest.of(1, 1, Sort.Direction.DESC, column);
        List<OrderProduct> orderProducts = List.of(givenOrderItemForExistingOrder(givenProduct(11, "someName", 15f, 10f), 15, 11));
        List<Order> orders = new ArrayList<>();
        orders.add(Order.builder()
                .id(1)
                .createdAt(dateFrom.plusHours(2))
                .client(AggregateReference.to(clientId))
                .totalSum(300f)
                .products(orderProducts)
                .build());
        Page<@NonNull Order> orderPage = new PageImpl<>(orders, pageable, pageable.getOffset());
        PaginationDto pagination = new PaginationDto(1, 1, 0);
        SortingDto sorting = new SortingDto(column, SortingDto.Direction.DESC);
        when(orderService.findOrdersByPeriod(eq(dateFrom), eq(dateTo), eq(clientId), eq(1), eq(1), eq("DESC"), eq(column)))
                .thenReturn(orderPage);
        //ACT & ASSERT
        mockMvc.perform(post("/order/getOrdersByPeriod")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someGetByPeriodRequest(from, to, clientId, pagination, sorting)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].id").value(1));
    }

    @Test
    @WithMockUser("spring")
    void test_getOrdersByPeriod_pageIsLessThanZero_responseBadRequest() throws Exception {
        //ARRANGE
        long from = 1615986580054L;
        long to = 1615986589387L;
        String column = "id";
        PaginationDto pagination = new PaginationDto(-1, 1, 0);
        SortingDto sorting = new SortingDto(column, SortingDto.Direction.DESC);
        //ACT & ASSERT
        mockMvc.perform(post("/order/getOrdersByPeriod")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someGetByPeriodRequest(from, to, 15, pagination, sorting)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("PAGE_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @Test
    @WithMockUser("spring")
    void test_getOrdersByPeriod_perPageIsLessThanZero_responseBadRequest() throws Exception {
        //ARRANGE
        long from = 1615986580054L;
        long to = 1615986589387L;
        String column = "id";
        PaginationDto pagination = new PaginationDto(1, -1, 0);
        SortingDto sorting = new SortingDto(column, SortingDto.Direction.DESC);
        //ACT & ASSERT
        mockMvc.perform(post("/order/getOrdersByPeriod")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someGetByPeriodRequest(from, to, 15, pagination, sorting)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("PAGE_SIZE_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @Test
    @WithMockUser("spring")
    void test_getOrdersByPeriod_perPageIsLessThanZeroAndPageIsLessThanZero_responseBadRequest() throws Exception {
        //ARRANGE
        long from = 1615986580054L;
        long to = 1615986589387L;
        String column = "id";
        PaginationDto pagination = new PaginationDto(-1, -1, 0);
        SortingDto sorting = new SortingDto(column, SortingDto.Direction.DESC);
        //ACT & ASSERT
        mockMvc.perform(post("/order/getOrdersByPeriod")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someGetByPeriodRequest(from, to, 15, pagination, sorting)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("PAGE_IS_LESS_OR_EQUAL_TO_ZERO"))
                .andExpect(jsonPath("$[1].code").value("PAGE_SIZE_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @Test
    @WithMockUser("spring")
    void test_updateOrder_responseOk() throws Exception {
        //ARRANGE & ACT & ASSERT
        mockMvc.perform(put("/order/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someUpdateOrderRequest(1, 2, 5.5f)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser("spring")
    void test_updateOrder_quantityIsLessThanZero_responseBadRequest() throws Exception {
        //ARRANGE & ACT & ASSERT
        mockMvc.perform(put("/order/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(someUpdateOrderRequest(1, 2, -5.5f)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO"));
    }

    @Test
    @WithMockUser("someUser")
    void test_placeOrder_responseOk() throws Exception {
        mockMvc.perform(put("/order/placeOrder/1"))
                .andExpect(status().isOk());

        verify(orderService).placeOrder(eq(1));
    }

    @Test
    @WithMockUser("someUser")
    void test_placeOrder_noOrderId_badRequest() throws Exception {
        mockMvc.perform(put("/order/placeOrder/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].code").value("ORDER_NOT_FOUND"));

        verify(orderService, never()).placeOrder(eq(1));
    }

    private void givenNewOrder(float productQuantity, int clientId) throws Exception {
        Product someProduct = givenProduct(11, "someName", 10f, 1.5f);
        OrderProduct givenOrderProduct = givenOrderProductForNewOrder(someProduct, productQuantity);
        List<OrderProduct> givenProducts = List.of(givenOrderProduct);
        Order givenOrder = Order.builder()
                .id(2)
                .products(givenProducts)
                .client(AggregateReference.to(clientId))
                .totalSum(400f)
                .createdAt(OffsetDateTime.now())
                .build();
        when(orderService.addProductToOrder(eq(0), eq(someProduct.getId()), eq(productQuantity), eq(clientId)))
                .thenReturn(givenOrder);
    }

    private void givenExistingOrder(int orderId, float givenQuantity, int clientId) throws Exception {
        Product someProduct1 = givenProduct(11, "someName", 10f, 1.5f);
        Product someProduct2 = givenProduct(12, "someName2", 10f, 1.5f);
        List<OrderProduct> givenProducts = List.of(
                givenOrderItemForExistingOrder(someProduct1, givenQuantity, 11),
                givenOrderItemForExistingOrder(someProduct2, givenQuantity, 12)
        );
        Order givenOrder = Order.builder()
                .id(orderId)
                .products(givenProducts)
                .client(AggregateReference.to(clientId))
                .totalSum(400f)
                .createdAt(OffsetDateTime.now())
                .build();
        when(orderService.addProductToOrder(eq(orderId), eq(someProduct2.getId()), eq(givenQuantity), eq(clientId)))
                .thenReturn(givenOrder);
    }

    private OrderProduct givenOrderProductForNewOrder(Product product, float quantity) {
        return OrderProduct.builder()
                .id(10)
                .product(AggregateReference.to(product.getId()))
                .quantity(quantity)
                .price(product.getPrice())
                .sum(product.getPrice() * quantity)
                .weight(product.getWeight() * quantity)
                .build();
    }


    private OrderProduct givenOrderItemForExistingOrder(Product product, float quantity, int orderProductId) {
        return OrderProduct.builder()
                .id(orderProductId)
                .product(AggregateReference.to(product.getId()))
                .quantity(quantity)
                .price(product.getPrice())
                .sum(product.getPrice() * quantity)
                .weight(product.getWeight() * quantity)
                .build();
    }

    private Product givenProduct(int id, String name, Float price, Float weight) {
        return Product.builder()
                .id(id)
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

    private String someAddProductToOrderRequest(int orderId, int productId, Float quantity, Integer clientId) {
        AddProductToOrderRequestDto request = new AddProductToOrderRequestDto(
                orderId,
                productId,
                quantity,
                clientId);
        return objectMapper.writeValueAsString(request);
    }

    private String someGetByPeriodRequest(long from, long to, int clientId, PaginationDto pagination, SortingDto sorting) {
        OrderListRequestDto request = new OrderListRequestDto(
                from, to, clientId, pagination, sorting
        );
        return objectMapper.writeValueAsString(request);
    }

    private String someUpdateOrderRequest(int orderId, int productId, float newQuantity) {
        UpdateOrderRequestDto request = new UpdateOrderRequestDto(
                orderId,
                productId,
                newQuantity);
        return objectMapper.writeValueAsString(request);
    }

}
