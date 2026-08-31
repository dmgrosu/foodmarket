package md.ramaiana.foodmarket.domain.auth.core.response;

import lombok.NonNull;

/**
 * Answer to a password reset request.
 * <p>
 * {@code resetEmailSent} is {@code true} even when nothing was sent — no such user, a user who
 * cannot sign in anyway, or a request inside the cooldown. That reads like a lie, and it is
 * deliberate: this endpoint is unauthenticated, so a field that distinguished those cases would hand
 * any caller an account-existence oracle. The one thing it does report honestly is a Mailjet failure
 * for a request that genuinely should have sent, so the frontend can offer a retry.
 */
public record PasswordResetInitiateResponse(
    @NonNull String email,
    boolean resetEmailSent
) {
}
