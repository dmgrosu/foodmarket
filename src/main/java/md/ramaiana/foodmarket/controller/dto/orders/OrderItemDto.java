package md.ramaiana.foodmarket.controller.dto.orders;

public record OrderItemDto(
        Integer productId,
        String productName,
        float quantity,
        float price,
        float sum,
        float weight
) {
}
