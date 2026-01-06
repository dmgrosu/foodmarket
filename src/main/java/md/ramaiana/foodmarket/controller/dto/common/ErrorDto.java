package md.ramaiana.foodmarket.controller.dto.common;

public record ErrorDto(
        ErrorCode code,
        String message
) {
}
