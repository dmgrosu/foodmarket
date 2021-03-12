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
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

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
        ResponseEntity<String> validation = validateRequest(addGoodToOrderRequest);
        if (validation != null) {
            return validation;
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
            return ResponseEntity.ok(printer.print(buildGoodNotFoundResponse()));
        } catch (ClientNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.ok(printer.print(buildClientNotFountResponse()));
        }
        return ResponseEntity.ok(printer.print(buildProtoFromDomain(order)));
    }

    @GetMapping("/getById")
    public ResponseEntity<?> getOrderById(@RequestParam("id") Integer orderId) throws InvalidProtocolBufferException {
        if (orderId == 0) {
            return ResponseEntity.badRequest().body(printer.print(buildOrderIdIsZeroResponse()));
        }
        Order order;
        try {
            order = orderService.findOrdersById(orderId);
        } catch (OrderNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.ok(printer.print(buildOrderNotFoundResponse()));
        } catch (OrderIdZeroException e) {
            log.warn(e.getMessage());
            return ResponseEntity.ok(printer.print(buildOrderIdIsZeroResponse()));
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.ok(printer.print(buildOrderIdIsNullResponse()));
        }
        return ResponseEntity.ok(printer.print(buildProtoFromDomain(order)));
    }

    @PostMapping("/deleteById")
    public ResponseEntity<?> deleteOrderById(@RequestBody Orders.DeleteOrderRequest deleteOrderRequest) {
        int orderId = deleteOrderRequest.getOrderId();
        orderService.deleteOrderById(orderId);
        return ResponseEntity.ok().build();
    }



    private Orders.AddGoodToOrderResponse buildProtoFromDomain(Order order) {
        Orders.OrderState state = Orders.OrderState.NEW;
        if (!StringUtils.hasText(order.getProcessingResult())) {
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

    private Common.ErrorResponse buildQuantityIsZeroResponse() {
        return Common.ErrorResponse.newBuilder()
                .setCode(Common.ErrorCode.QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO)
                .build();
    }

    private Common.ErrorResponse buildGoodIsZeroResponse() {
        return Common.ErrorResponse.newBuilder()
                .setCode(Common.ErrorCode.GOOD_ID_IS_LESS_OR_EQUAL_TO_ZERO)
                .build();
    }

    private Common.ErrorResponse buildGoodNotFoundResponse() {
        return Common.ErrorResponse.newBuilder()
                .setCode(Common.ErrorCode.GOOD_NOT_FOUND)
                .build();
    }

    private Common.ErrorResponse buildClientNotFountResponse() {
        return Common.ErrorResponse.newBuilder()
                .setCode(Common.ErrorCode.CLIENT_NOT_FOUND)
                .build();
    }

    private Common.ErrorResponse buildOrderNotFoundResponse() {
        return Common.ErrorResponse.newBuilder()
                .setCode(Common.ErrorCode.ORDER_NOT_FOUND)
                .build();
    }

    private Common.ErrorResponse buildOrderIdIsZeroResponse() {
        return Common.ErrorResponse.newBuilder()
                .setCode(Common.ErrorCode.ORDER_ID_IS_ZERO)
                .build();
    }

    private Common.ErrorResponse buildOrderIdIsNullResponse() {
        return Common.ErrorResponse.newBuilder()
                .setCode(Common.ErrorCode.ORDER_ID_IS_NULL)
                .build();
    }

    private ResponseEntity<String> validateRequest(Orders.AddGoodToOrderRequest addGoodToOrderRequest) throws InvalidProtocolBufferException {
        int goodId = addGoodToOrderRequest.getGoodId();
        float quantity = addGoodToOrderRequest.getQuantity();
        if (quantity <= 0) {
            return ResponseEntity.badRequest().body(printer.print(buildQuantityIsZeroResponse()));
        }
        if (goodId <= 0) {
            return ResponseEntity.badRequest().body(printer.print(buildGoodIsZeroResponse()));
        }
        return null;
    }
}