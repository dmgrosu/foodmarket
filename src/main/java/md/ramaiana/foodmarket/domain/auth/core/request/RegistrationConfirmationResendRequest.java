package md.ramaiana.foodmarket.domain.auth.core.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * Resend registration confirmation request.
 */
public record RegistrationConfirmationResendRequest(
        @NonNull
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
}
