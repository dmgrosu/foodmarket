package md.ramaiana.foodmarket.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderGood;
import md.ramaiana.foodmarket.model.OrderState;
import md.ramaiana.foodmarket.proto.Clients;
import md.ramaiana.foodmarket.proto.Common;
import md.ramaiana.foodmarket.proto.Common.ErrorCode;
import md.ramaiana.foodmarket.proto.Orders;
import md.ramaiana.foodmarket.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final GoodService goodService;
    private final JsonFormat.Printer printer;

    @Autowired
    public OrderController(OrderService orderService, GoodService goodService) {
        this.orderService = orderService;
        this.goodService = goodService;
        this.printer = JsonFormat.printer().omittingInsignificantWhitespace();
    }

    @PostMapping("/addGood")
    public ResponseEntity<?> addGoodToOrder(@RequestBody Orders.AddGoodToOrderRequest addGoodToOrderRequest) throws InvalidProtocolBufferException {
        try {
            List<Common.Error> errors = validateAddGoodToOrderRequest(addGoodToOrderRequest);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(printer.print(buildErrorsResponse(errors)));
            }
            int orderId = addGoodToOrderRequest.getOrderId();
            int goodId = addGoodToOrderRequest.getGoodId();
            float quantity = addGoodToOrderRequest.getQuantity();
            int clientId = addGoodToOrderRequest.getClientId();
            Order order = orderService.addGoodToOrder(orderId, goodId, quantity, clientId);
            return ResponseEntity.ok(printer.print(buildProtoFromDomain(order)));
        } catch (GoodNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), ErrorCode.GOOD_NOT_FOUND)));
        } catch (ClientNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), ErrorCode.CLIENT_NOT_FOUND)));
        } catch (OrderAlreadyProcessedException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_ALREADY_PROCESSED)));
        } catch (OrderNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_NOT_FOUND)));
        } catch (Exception e) {
            return internalErrorResponse(e);
        }
    }

    @GetMapping("/getById")
    public ResponseEntity<?> getOrderById(@RequestBody Orders.GetOrderByIdRequest getOrderByIdRequest) throws InvalidProtocolBufferException {
        try {
            int orderId = getOrderByIdRequest.getOrderId();
            if (orderId == 0) {
                return ResponseEntity.badRequest().body(printer.print(buildErrorResponse("Order ID is zero", ErrorCode.ORDER_ID_IS_ZERO)));
            }
            Order order = orderService.findOrdersById(orderId);
            return ResponseEntity.ok(printer.print(buildProtoFromDomain(order)));
        } catch (OrderNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_NOT_FOUND)));
        } catch (OrderIdZeroException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_ID_IS_ZERO)));
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_ID_IS_NULL)));
        } catch (Exception e) {
            return internalErrorResponse(e);
        }
    }

    @PostMapping("/deleteById")
    public ResponseEntity<?> deleteOrderById(@RequestBody Orders.DeleteOrderRequest deleteOrderRequest) {
        int orderId = deleteOrderRequest.getOrderId();
        orderService.deleteOrderById(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deleteGood")
    public ResponseEntity<?> deleteGoodFromOrder(@RequestBody Orders.DeleteGoodFromOrderRequest deleteGoodFromOrderRequest) throws InvalidProtocolBufferException {
        int orderGoodId = deleteGoodFromOrderRequest.getOrderGoodId();
        int orderId = deleteGoodFromOrderRequest.getOrderId();
        if (orderGoodId == 0) {
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse("Invalid orderGoodId", ErrorCode.ORDER_NOT_FOUND)));
        }
        try {
            orderService.deleteGoodFromOrder(orderId, orderGoodId);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_NOT_FOUND)));
        } catch (Exception e) {
            return internalErrorResponse(e);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/getOrdersByPeriod")
    public ResponseEntity<?> getOrdersByPeriod(@RequestBody Orders.OrderListRequest orderListRequest) throws InvalidProtocolBufferException {
        try {
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
            Page<Order> orders = orderService.findOrdersByPeriod(dateFrom, dateTo, clientId, pageNumber, pageSize, direction, sortingColumnName);
            return ResponseEntity.ok(printer.print(buildListOrdersProtoFromDomain(orders)));
        } catch (ClientNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), ErrorCode.CLIENT_NOT_FOUND)));
        } catch (Exception e) {
            return internalErrorResponse(e);
        }
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
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse(e.getMessage(), ErrorCode.GOOD_NOT_FOUND)));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/placeOrder")
    public ResponseEntity<?> placeOrder(@RequestBody Orders.PlaceOrderRequest placeOrderRequest) throws InvalidProtocolBufferException {
        int orderId = placeOrderRequest.getOrderId();
        if (orderId == 0) {
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse("Missing required order ID", ErrorCode.ORDER_NOT_FOUND)));
        }
        try {
            orderService.placeOrder(orderId);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.badRequest().body(printer.print(buildErrorResponse("Missing required order ID", ErrorCode.ORDER_NOT_FOUND)));
        } catch (Exception e) {
            internalErrorResponse(e);
        }
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<?> internalErrorResponse(Exception e) throws InvalidProtocolBufferException {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(500).body(printer.print(buildErrorResponse(e.getMessage(), ErrorCode.INTERNAL_SERVER_ERROR)));
    }

    private Orders.OrdersListResponse buildListOrdersProtoFromDomain(Page<Order> orders) {
        List<Orders.Order> protoOrders = new ArrayList<>();
        for (Order order : orders.toList()) {
            Orders.OrderState state = Orders.OrderState.NEW;
            List<Orders.OrderGood> protoGoods = new ArrayList<>();
            for (OrderGood good : order.getGoods()) {
                protoGoods.add(mapOrderGoodToProto(good));
            }
            protoOrders.add(Orders.Order.newBuilder()
                    .setId(order.getId())
                    .setClient(Clients.Client.newBuilder().setId(order.getClientId()).build())
                    .setTotalSum(order.getTotalSum())
                    .setState(mapOrderStateToProto(order.getState()))
                    .setCreatedAt(order.getCreatedAt().toInstant().toEpochMilli())
                    .setTotalWeight(order.getTotalWeightForGoods())
                    .addAllGoods(protoGoods)
                    .build());
        }
        Common.Pagination protoPagination = Common.Pagination.newBuilder()
                .setPageNo(orders.getNumber())
                .setPerPage(orders.getSize())
                .setTotalCount(orders.getTotalElements())
                .build();
        return Orders.OrdersListResponse.newBuilder()
                .addAllOrders(protoOrders)
                .setPagination(protoPagination)
                .build();
    }

    private Orders.OrderState mapOrderStateToProto(OrderState state) {
        switch (state) {
            case PLACED:
                return Orders.OrderState.PLACED;
            case PROCESSED:
                return Orders.OrderState.PROCESSED;
            case NOT_PROCESSED:
                return Orders.OrderState.NOT_PROCESSED;
            default:
                return Orders.OrderState.NEW;
        }
    }

    private Orders.AddGoodToOrderResponse buildProtoFromDomain(Order order) {
        Orders.OrderState state = Orders.OrderState.NEW;
        if (StringUtils.hasText(order.getProcessingResult())) {
            state = Orders.OrderState.PROCESSED;
            // TODO: 3/5/2021 Make logic
        }
        Clients.Client protoClient = Clients.Client.newBuilder().setId(order.getClientId()).build();
        List<Orders.OrderGood> protoGoods = new ArrayList<>();
        Set<OrderGood> goods = order.getGoods();
        for (OrderGood good : goods) {
            protoGoods.add(mapOrderGoodToProto(good));
        }
        Orders.Order protoOrder = Orders.Order.newBuilder()
                .setId(order.getId())
                .setClient(protoClient)
                .addAllGoods(protoGoods)
                .setState(state)
                .setCreatedAt(order.getCreatedAt().toInstant().toEpochMilli())
                .setTotalWeight(order.getTotalWeightForGoods())
                .setTotalSum(order.getTotalSum())
                .build();

        return Orders.AddGoodToOrderResponse.newBuilder()
                .setOrder(protoOrder)
                .build();
    }

    private Orders.OrderGood mapOrderGoodToProto(OrderGood good) {
        String goodName = goodService.getGoodNameById(good.getGoodId());
        return Orders.OrderGood.newBuilder()
                .setGoodId(good.getId())
                .setQuantity(good.getQuantity())
                .setSum(good.getSum())
                .setWeight(good.getWeight())
                .setGoodName(goodName == null ? "" : goodName)
                .build();
    }

    private Common.ErrorResponse buildErrorResponse(String description, ErrorCode code) {
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
                    .setCode(ErrorCode.QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO)
                    .build());
        }
        if (goodId <= 0) {
            errors.add(Common.Error.newBuilder()
                    .setCode(ErrorCode.GOOD_ID_IS_LESS_OR_EQUAL_TO_ZERO)
                    .build());
        }
        return errors;
    }

    private List<Common.Error> validateOrderListRequest(Orders.OrderListRequest request) {
        List<Common.Error> errors = new ArrayList<>();
        int pageNumber = request.getPagination().getPageNo();
        int perPage = request.getPagination().getPerPage();
        if (pageNumber < 0) {
            errors.add(Common.Error.newBuilder()
                    .setCode(ErrorCode.PAGE_IS_LESS_OR_EQUAL_TO_ZERO)
                    .build());
        }
        if (perPage < 0) {
            errors.add(Common.Error.newBuilder()
                    .setCode(ErrorCode.PAGE_SIZE_IS_LESS_OR_EQUAL_TO_ZERO)
                    .build());
        }
        return errors;
    }

    private List<Common.Error> validateUpdateOrderRequest(Orders.UpdateOrderRequest request) {
        List<Common.Error> errors = new ArrayList<>();
        float quantity = request.getNewQuantity();
        if (quantity <= 0) {
            errors.add(Common.Error.newBuilder()
                    .setCode(ErrorCode.QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO)
                    .build());
        }
        return errors;
    }
}
