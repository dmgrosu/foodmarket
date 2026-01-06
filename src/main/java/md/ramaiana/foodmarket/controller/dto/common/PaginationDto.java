package md.ramaiana.foodmarket.controller.dto.common;

public record PaginationDto(
        int pageNo,
        int pageSize,
        int totalCount
) {
}
