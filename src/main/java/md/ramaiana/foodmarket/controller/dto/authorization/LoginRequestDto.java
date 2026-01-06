package md.ramaiana.foodmarket.controller.dto.authorization;

public record LoginRequestDto(
        String email,
        String password
) {
}
