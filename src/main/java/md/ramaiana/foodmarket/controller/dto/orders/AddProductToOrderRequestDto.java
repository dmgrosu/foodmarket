package md.ramaiana.foodmarket.controller.dto.orders;

public record AddProductToOrderRequestDto(
        Integer orderId,
        int productId,
        float quantity,
        int clientId
) {
}
