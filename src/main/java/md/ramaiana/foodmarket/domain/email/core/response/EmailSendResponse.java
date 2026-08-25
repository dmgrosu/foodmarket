package md.ramaiana.foodmarket.domain.email.core.response;

import lombok.NonNull;

/**
 * Result of sending a templated email via Mailjet.
 */
public record EmailSendResponse(
    @NonNull String messageUuid,
    @NonNull String recipientEmail
) {
}
