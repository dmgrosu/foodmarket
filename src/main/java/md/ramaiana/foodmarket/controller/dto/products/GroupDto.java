package md.ramaiana.foodmarket.controller.dto.products;

import java.util.List;

public record GroupDto(
        int id,
        String name,
        List<GroupDto> groups,
        List<ProductDto> products
) {
}
