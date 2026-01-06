package md.ramaiana.foodmarket.controller.dto.orders;

import md.ramaiana.foodmarket.controller.dto.common.PaginationDto;
import md.ramaiana.foodmarket.controller.dto.common.SortingDto;

public record OrderListRequestDto(
        long dateFrom, // date in millis
        long dateTo, // date in millis
        int clientId,
        PaginationDto pagination,
        SortingDto sorting
) {
}
