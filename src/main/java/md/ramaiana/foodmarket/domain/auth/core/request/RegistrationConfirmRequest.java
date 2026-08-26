package md.ramaiana.foodmarket.domain.auth.core.request;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * Confirm registration request.
 */
public record RegistrationConfirmRequest(
        @NonNull
        @NotBlank(message = "Confirmation token is required")
        String confirmationToken
) {
}
