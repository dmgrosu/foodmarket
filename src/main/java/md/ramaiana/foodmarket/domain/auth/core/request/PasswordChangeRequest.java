package md.ramaiana.foodmarket.domain.auth.core.request;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

/**
 * Change the signed-in user's own password. Requires the current one, so a borrowed session cannot
 * lock the real owner out.
 * <p>
 * Deliberately no strength constraint here: the backend enforces none at registration either, and a
 * rule on this endpoint alone would let a user register with a password they could then never re-set.
 */
public record PasswordChangeRequest(

    @NonNull
    @NotBlank(message = "Current password is required")
    String currentPassword,

    @NonNull
    @NotBlank(message = "New password is required")
    String newPassword
) {
}
