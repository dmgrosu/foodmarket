package md.ramaiana.foodmarket.domain.email.core.request;

import lombok.NonNull;

/**
 * Email recipient: email address and display name.
 */
public record EmailRecipient(
    @NonNull String email,
    String name
) {
    public EmailRecipient {
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be blank");
        }
        if (name != null && name.isBlank()) {
            name = null;
        }
    }
}
