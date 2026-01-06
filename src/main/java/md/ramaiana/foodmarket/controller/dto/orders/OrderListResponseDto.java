package md.ramaiana.foodmarket.controller.dto.orders;

import md.ramaiana.foodmarket.controller.dto.common.PaginationDto;

import java.util.List;

public record OrderListResponseDto(
    List<OrderDto> orders,
    PaginationDto pagination
) {
}
