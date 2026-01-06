package md.ramaiana.foodmarket.controller;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import md.ramaiana.foodmarket.controller.dto.ClientDto;
import md.ramaiana.foodmarket.controller.dto.common.ErrorCode;
import md.ramaiana.foodmarket.controller.dto.common.ErrorDto;
import md.ramaiana.foodmarket.controller.dto.common.PaginationDto;
import md.ramaiana.foodmarket.controller.dto.orders.*;
import md.ramaiana.foodmarket.model.Order;
import md.ramaiana.foodmarket.model.OrderProduct;
import md.ramaiana.foodmarket.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
    private final ProductService productService;

    @Autowired
    public OrderController(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    @PostMapping("/addProduct")
    public ResponseEntity<?> addProductToOrder(@RequestBody AddProductToOrderRequestDto request) {
        try {
            List<ErrorDto> errors = validateAddProductToOrderRequest(request);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(errors);
            }
            Order order = orderService.addProductToOrder(
                    request.orderId(),
                    request.productId(),
                    request.quantity(),
                    request.clientId()
            );
            return ResponseEntity.ok(toOrderDto(order));
        } catch (ProductNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage(), ErrorCode.GOOD_NOT_FOUND));
        } catch (ClientNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage(), ErrorCode.CLIENT_NOT_FOUND));
        } catch (OrderAlreadyProcessedException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_ALREADY_PROCESSED));
        } catch (OrderNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_NOT_FOUND));
        } catch (Exception e) {
            return internalErrorResponse(e);
        }
    }

    @GetMapping("/getById/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable int orderId) {
        try {
            if (orderId == 0) {
                return ResponseEntity.badRequest().body(buildErrorResponse("Order ID is zero", ErrorCode.ORDER_ID_IS_ZERO));
            }
            Order order = orderService.findOrdersById(orderId);
            return ResponseEntity.ok(toOrderDto(order));
        } catch (OrderNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_NOT_FOUND));
        } catch (OrderIdZeroException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_ID_IS_ZERO));
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_ID_IS_NULL));
        } catch (Exception e) {
            return internalErrorResponse(e);
        }
    }

    @DeleteMapping("/deleteById/{orderId}")
    public ResponseEntity<?> deleteOrderById(@PathVariable int orderId) {
        orderService.deleteOrderById(orderId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deleteProduct/{orderId}/{itemId}")
    public ResponseEntity<?> deleteProductFromOrder(@PathVariable int orderId,
                                                    @PathVariable int itemId) {
        if (itemId == 0) {
            return ResponseEntity.badRequest().body(buildErrorResponse("Invalid orderProductId", ErrorCode.ORDER_NOT_FOUND));
        }
        try {
            orderService.deleteProductFromOrder(orderId, itemId);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_NOT_FOUND));
        } catch (Exception e) {
            return internalErrorResponse(e);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/getOrdersByPeriod")
    public ResponseEntity<?> getOrdersByPeriod(@RequestBody OrderListRequestDto request) {
        try {
            List<ErrorDto> errors = validateOrderListRequest(request);
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(errors);
            }
            long from = request.dateFrom();
            long to = request.dateTo();
            OffsetDateTime dateFrom = OffsetDateTime.ofInstant(Instant.ofEpochMilli(from), ZoneId.of("UTC"));
            OffsetDateTime dateTo = OffsetDateTime.ofInstant(Instant.ofEpochMilli(to), ZoneId.of("UTC"));
            Page<@NonNull Order> orders = orderService.findOrdersByPeriod(dateFrom, dateTo,
                    request.clientId(),
                    request.pagination().pageNo(),
                    request.pagination().pageSize(),
                    request.sorting().direction().toString(),
                    request.sorting().columnName()
            );
            return ResponseEntity.ok(buildListOrdersProtoFromDomain(orders));
        } catch (ClientNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage(), ErrorCode.CLIENT_NOT_FOUND));
        } catch (Exception e) {
            return internalErrorResponse(e);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateOrder(@RequestBody UpdateOrderRequestDto request) {
        List<ErrorDto> errors = validateUpdateOrderRequest(request);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            orderService.updateProductQuantity(
                    request.orderId(),
                    request.productId(),
                    request.quantity()
            );
        } catch (OrderNotFoundException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage(), ErrorCode.ORDER_NOT_FOUND));
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/placeOrder/{orderId}")
    public ResponseEntity<?> placeOrder(@PathVariable int orderId) {
        if (orderId == 0) {
            return ResponseEntity.badRequest().body(buildErrorResponse("Missing required order ID", ErrorCode.ORDER_NOT_FOUND));
        }
        try {
            orderService.placeOrder(orderId);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.badRequest().body(buildErrorResponse("Missing required order ID", ErrorCode.ORDER_NOT_FOUND));
        } catch (Exception e) {
            internalErrorResponse(e);
        }
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<?> internalErrorResponse(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(500).body(buildErrorResponse(e.getMessage(), ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private OrderListResponseDto buildListOrdersProtoFromDomain(Page<@NonNull Order> orders) {
        List<OrderDto> orderDto = orders.stream()
                .map(this::toOrderDto)
                .toList();
        PaginationDto paginationDto = new PaginationDto(
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalPages());
        return new OrderListResponseDto(orderDto, paginationDto);
    }

    private OrderDto toOrderDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getTotalSum(),
                new ClientDto(order.getClient().getId(), null, null),
                order.getState(),
                order.getCreatedAt().toInstant().toEpochMilli(),
                order.getTotalWeightForProducts(),
                order.getProducts().stream()
                        .map(this::toItemDto)
                        .toList()
        );
    }

    private OrderItemDto toItemDto(OrderProduct orderProduct) {
        String productName = productService.getProductNameById(orderProduct.getProduct().getId());
        return new OrderItemDto(
                orderProduct.getId(),
                productName == null ? "" : productName,
                orderProduct.getQuantity(),
                orderProduct.getPrice(),
                orderProduct.getSum(),
                orderProduct.getWeight()
        );
    }

    private List<ErrorDto> buildErrorResponse(String description, ErrorCode code) {
        return List.of(new ErrorDto(code, description));
    }

    private List<ErrorDto> validateAddProductToOrderRequest(AddProductToOrderRequestDto request) {
        List<ErrorDto> errors = new ArrayList<>();
        int productId = request.productId();
        float quantity = request.quantity();
        if (quantity <= 0) {
            errors.add(new ErrorDto(ErrorCode.QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO, null));
        }
        if (productId <= 0) {
            errors.add(new ErrorDto(ErrorCode.GOOD_ID_IS_LESS_OR_EQUAL_TO_ZERO, null));
        }
        return errors;
    }

    private List<ErrorDto> validateOrderListRequest(OrderListRequestDto request) {
        List<ErrorDto> errors = new ArrayList<>();
        int pageNumber = request.pagination().pageNo();
        int pageSize = request.pagination().pageSize();
        if (pageNumber <= 0) {
            errors.add(new ErrorDto(ErrorCode.PAGE_IS_LESS_OR_EQUAL_TO_ZERO, null));
        }
        if (pageSize <= 0) {
            errors.add(new ErrorDto(ErrorCode.PAGE_SIZE_IS_LESS_OR_EQUAL_TO_ZERO, null));
        }
        return errors;
    }

    private List<ErrorDto> validateUpdateOrderRequest(UpdateOrderRequestDto request) {
        List<ErrorDto> errors = new ArrayList<>();
        float quantity = request.quantity();
        if (quantity <= 0) {
            errors.add(new ErrorDto(ErrorCode.QUANTITY_IS_LESS_OR_EQUAL_TO_ZERO, null));
        }
        return errors;
    }
}
