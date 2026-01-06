package md.ramaiana.foodmarket.controller.dto.authorization;

public record SignUpRequestDto(
        String email,
        String password,
        Integer clientId
) {
}
