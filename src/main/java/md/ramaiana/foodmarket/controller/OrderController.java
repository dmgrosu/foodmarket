package md.ramaiana.foodmarket.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderGood;
import md.ramaiana.foodmarket.proto.Clients;
import md.ramaiana.foodmarket.proto.Common;
import md.ramaiana.foodmarket.proto.Goods;
import md.ramaiana.foodmarket.proto.Orders;
import md.ramaiana.foodmarket.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final JsonFormat.Printer printer;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
        this.printer = JsonFormat.printer().omittingInsignificantWhitespace();
    }

    @PostMapping("/save")
    public ResponseEntity<?> addGoodToOrder(@RequestBody Orders.AddGoodToOrderRequest addGoodToOrderRequest) throws InvalidProtocolBufferException {
        //validation
        List<Common.Error> errors = validateAddGoodToOrderRequest(addGoodToOrderRequest);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(printer.print(buildErrorsResponse(errors)));
        }
        int orderId = addGoodToOrderRequest.getOrderId();
        int goodId = addGoodToOrderRequest.getGoodId();
        float quantity = addGoodToOrderRequest.getQuantity();
        int clientId = addGoodToOrderRequest.getClientId();
        Order order;
        try {
            order = orderService.addGoodToOrder(orderId, goodId, quantity, clientId);
        } catch (GoodNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), Common.ErrorCode.GOOD_NOT_FOUND)));
        } catch (ClientNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), Common.ErrorCode.CLIENT_NOT_FOUND)));
        } catch (OrderAlreadyProcessedException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), Common.ErrorCode.ORDER_ALREADY_PROCESSED)));
        }
        return ResponseEntity.ok(printer.print(buildProtoFromDomain(order)));
    }

    @GetMapping("/getById")
    public ResponseEntity<?> getOrderById(@RequestBody Orders.GetOrderByIdRequest getOrderByIdRequest) throws InvalidProtocolBufferException {
        int orderId = getOrderByIdRequest.getOrderId();
        //validation
        if (orderId == 0) {
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse("Order ID is zero", Common.ErrorCode.ORDER_ID_IS_ZERO)));
        }
        Order order;
        try {
            order = orderService.findOrdersById(orderId);
        } catch (OrderNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), Common.ErrorCode.ORDER_NOT_FOUND)));
        } catch (OrderIdZeroException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), Common.ErrorCode.ORDER_ID_IS_ZERO)));
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), Common.ErrorCode.ORDER_ID_IS_NULL)));
        }
        return ResponseEntity.ok(printer.print(buildProtoFromDomain(order)));
    }

    @PostMapping("/deleteById")
    public ResponseEntity<?> deleteOrderById(@RequestBody Orders.DeleteOrderRequest deleteOrderRequest) {
        int orderId = deleteOrderRequest.getOrderId();
        orderService.deleteOrderById(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/getOrdersByPeriod")
    public ResponseEntity<?> getOrdersByPeriod(@RequestBody Orders.OrderListRequest orderListRequest) throws InvalidProtocolBufferException {
        List<Common.Error> errors = validateOrderListRequest(orderListRequest);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(printer.print(buildErrorsResponse(errors)));
        }
        long from = orderListRequest.getDateFrom();
        long to = orderListRequest.getDateTo();
        OffsetDateTime dateFrom = OffsetDateTime.ofInstant(Instant.ofEpochMilli(from), ZoneId.of("UTC"));
        OffsetDateTime dateTo = OffsetDateTime.ofInstant(Instant.ofEpochMilli(to), ZoneId.of("UTC"));
        Integer clientId = orderListRequest.getClientId();
        Integer pageNumber = orderListRequest.getPagination().getPageNo();
        Integer pageSize = orderListRequest.getPagination().getPerPage();
        String sortingColumnName = orderListRequest.getSorting().getColumnName();
        String direction = orderListRequest.getSorting().getDirection().toString();
        Page<Order> orders;
        try {
            orders = orderService.findOrdersByPeriod(dateFrom, dateTo, clientId, pageNumber, pageSize, direction, sortingColumnName);
        } catch (ClientNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), Common.ErrorCode.CLIENT_NOT_FOUND)));
        }
        return ResponseEntity.ok(printer.print(buildListOrdersProtoFromDomain(orders)));
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateOrder(@RequestBody Orders.UpdateOrderRequest updateOrderRequest) throws InvalidProtocolBufferException {
        List<Common.Error> errors = validateUpdateOrderRequest(updateOrderRequest);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(printer.print(buildErrorsResponse(errors)));
        }
        int orderId = updateOrderRequest.getOrderId();
        int goodId = updateOrderRequest.getGoodId();
        float newQuantity = updateOrderRequest.getNewQuantity();
        try {
            orderService.updateOrder(orderId, goodId, newQuantity);
        } catch (GoodNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), Common.ErrorCode.GOOD_NOT_FOUND)));
        }
        return ResponseEntity.ok().build();
    }


    private Orders.OrdersListResponse buildListOrdersProtoFromDomain(Page<Order> orders) {
        List<Orders.Order> protoOrders = new ArrayList<>();
        for (Order order : orders.toList()) {
            Orders.OrderState state = Orders.OrderState.NEW;
            List<Goods.Good> protoGoods = new ArrayList<>();
            for (OrderGood good : order.getGoods()) {
                protoGoods.add(Goods.Good.newBuilder()
                        .setId(good.getId())
                        .setWeight(good.getWeight())
                        .build());
            }
            protoOrders.add(Orders.Order.newBuilder()
                    .setId(order.getId())
                    .setClient(Clients.Client.newBuilder().setId(order.getClientId()).build())
                    .setTotalSum(order.getTotalSumForGoods())
                    .setState(state)
                    .setDate(order.getCreatedAt().toInstant().toEpochMilli())
                    .setTotalWeight(order.getTotalWeightForGoods())
                    .addAllGoods(protoGoods)
                    .build());
        }
        Common.Pagination protoPagination = Common.Pagination.newBuilder()
                .setPageNo(orders.getNumber())
                .setPerPage(orders.getSize())
                .setTotalCount(orders.getTotalPages())
                .build();
        return Orders.OrdersListResponse.newBuilder()
                .addAllOrders(protoOrders)
                .setPagination(protoPagination)
                .build();
    }

    private Orders.AddGoodToOrderResponse buildProtoFromDomain(Order order) {
        Orders.OrderState state = Orders.OrderState.NEW;
        if (!StringUtils.hasText(order.getProcessingResult())) {
            state = Orders.OrderState.PROCESSED;
            // TODO: 3/5/2021 Make logic
        }
        Clients.Client protoClient = Clients.Client.newBuilder().setId(order.getClientId()).build();
        List<Goods.Good> protoGoods = new ArrayList<>();
        List<OrderGood> goods = order.getGoods();
        for (OrderGood good : goods) {
            protoGoods.add(Goods.Good.newBuilder()
                    .setId(good.getId())
                    .setWeight(good.getWeight())
                    .build());
        }
        Orders.Order protoOrder = Orders.Order.newBuilder()
                .setId(order.getId())
                .setClient(protoClient)
                .addAllGoods(protoGoods)
                .setState(state)
                .setDate(order.getCreatedAt().toInstant().toEpochMilli())
                .setTotalWeight(order.getTotalWeightForGoods())
                .setTotalSum(order.getTotalSumForGoods())
                .build();

        return Orders.AddGoodToOrderResponse.newBuilder()
                .setOrder(protoOrder)
                .build();
    }

    private Common.ErrorResponse buildErrorResponse(String description, Common.ErrorCode code) {
        return Common.ErrorResponse.newBuilder()
                .addErrors(Common.Error.newBuilder()
                        .setCode(code)
                        .setDescription(description)
                        .build())
                .build();
    }

    private Common.ErrorResponse buildErrorsResponse(List<Common.Error> errors) {
        return Common.ErrorResponse.newBuilder()
                .addAllErrors(errors)
                .build();
    }

    private List<Common.Error> validateAddGoodToOrderRequest(Orders.AddGoodToOrderRequest addGoodToOrderRequest) {
        List<Common.Error> errors = new ArrayList<>();
        int goodId = addGoodToOrderRequest.getGoodId();
        float quantity = addGoodToOrderRequest.getQuantity();
        if (quantity <= 0) {
            errors.add(Common.Error.newBuilder()
                    .setCode(Common.ErrorCode.QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO)
                    .build());
        }
        if (goodId <= 0) {
            errors.add(Common.Error.newBuilder()
                    .setCode(Common.ErrorCode.GOOD_ID_IS_LESS_OR_EQUAL_TO_ZERO)
                    .build());
        }
        return errors;
    }

    private List<Common.Error> validateOrderListRequest(Orders.OrderListRequest request) {
        List<Common.Error> errors = new ArrayList<>();
        int pageNumber = request.getPagination().getPageNo();
        int perPage = request.getPagination().getPerPage();
        if (pageNumber <= 0) {
            errors.add(Common.Error.newBuilder()
                    .setCode(Common.ErrorCode.PAGE_IS_LESS_OR_EQUAL_TO_ZERO)
                    .build());
        }
        if (perPage <= 0) {
            errors.add(Common.Error.newBuilder()
                    .setCode(Common.ErrorCode.PAGE_SIZE_IS_LESS_OR_EQUAL_TO_ZERO)
                    .build());
        }
        return errors;
    }

    private List<Common.Error> validateUpdateOrderRequest(Orders.UpdateOrderRequest request) {
        List<Common.Error> errors = new ArrayList<>();
        float quantity = request.getNewQuantity();
        if (quantity <= 0) {
            errors.add(Common.Error.newBuilder()
                    .setCode(Common.ErrorCode.QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO)
                    .build());
        }
        return errors;
    }
}
