package md.ramaiana.foodmarket.controller.dto.authorization;

import jakarta.annotation.Nullable;
import md.ramaiana.foodmarket.controller.dto.ClientDto;

public record UserDto(
        Integer id,
        String email,
        @Nullable
        ClientDto client
) {
}
