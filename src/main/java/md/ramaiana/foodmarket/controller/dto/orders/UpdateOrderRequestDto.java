package md.ramaiana.foodmarket.controller.dto.orders;

public record UpdateOrderRequestDto(
        int orderId,
        int productId,
        float quantity
) {
}
