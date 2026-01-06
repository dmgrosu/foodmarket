package md.ramaiana.foodmarket.controller.dto.orders;

import md.ramaiana.foodmarket.controller.dto.ClientDto;
import md.ramaiana.foodmarket.model.OrderState;

import java.util.List;

public record OrderDto(
        // null for new order
        Integer id,
        float totalSum,
        ClientDto client,
        OrderState state,
        // date in millis for UTC timezone
        long date,
        float totalWeight,
        List<OrderItemDto> items
) {
}
