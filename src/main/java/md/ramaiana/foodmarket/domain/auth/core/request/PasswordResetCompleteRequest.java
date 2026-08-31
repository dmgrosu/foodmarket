package md.ramaiana.foodmarket.domain.auth.core.request;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * Set a new password using the token from a reset email. The token is the credential, so no
 * current password is asked for — the user does not have it.
 */
public record PasswordResetCompleteRequest(
    @NonNull
    @NotBlank(message = "Reset token is required")
    String resetToken,

    @NonNull
    @NotBlank(message = "New password is required")
    String newPassword
) {
}
