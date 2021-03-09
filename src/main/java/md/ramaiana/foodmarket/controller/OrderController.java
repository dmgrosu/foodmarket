package md.ramaiana.foodmarket.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderGood;
import md.ramaiana.foodmarket.proto.Clients;
import md.ramaiana.foodmarket.proto.Common;
import md.ramaiana.foodmarket.proto.Goods;
import md.ramaiana.foodmarket.proto.Orders;
import md.ramaiana.foodmarket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order")
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
        Order order = orderService.addGoodToOrder(orderId, goodId, quantity, clientId);
        return ResponseEntity.ok(printer.print(buildProtoFromDomain(order)));
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
