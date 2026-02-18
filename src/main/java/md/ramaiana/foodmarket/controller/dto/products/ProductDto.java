package md.ramaiana.foodmarket.controller.dto.products;

import java.util.List;

public record ProductDto(
        int id,
        String name,
        int groupId,
        int brandId,
        float packSize, // quantity in one package
        String barCode,
        String unit, // unit of measurement
        float weight,
        List<PriceDto> prices
) {
}
