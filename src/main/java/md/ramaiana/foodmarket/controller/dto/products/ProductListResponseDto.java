package md.ramaiana.foodmarket.controller.dto.products;

import java.util.List;

public record ProductListResponseDto(
        List<ProductDto> products,
        List<GroupDto> groups
) {
}
