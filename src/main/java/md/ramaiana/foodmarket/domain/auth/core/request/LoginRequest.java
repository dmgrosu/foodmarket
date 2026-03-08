package md.ramaiana.foodmarket.domain.auth.core.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * Login request.
 */
public record LoginRequest(
        @NonNull
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        @NonNull
        @NotBlank(message = "Password is required")
        String password
) {
}
