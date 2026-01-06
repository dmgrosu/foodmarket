package md.ramaiana.foodmarket.controller.dto.authorization;

public record LoginResponseDto(
        UserDto user,
        String token,
        // token validity in seconds
        Integer tokenTtl
) {
}
