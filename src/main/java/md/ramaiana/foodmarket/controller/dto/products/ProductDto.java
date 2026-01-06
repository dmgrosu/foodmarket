package md.ramaiana.foodmarket.controller.dto.products;

public record ProductDto(
        int id,
        String name,
        float price,
        int groupId,
        int brandId,
        float packSize, // quantity in one package
        String barCode,
        String unit, // unit of measurement
        float weight
) {
}
