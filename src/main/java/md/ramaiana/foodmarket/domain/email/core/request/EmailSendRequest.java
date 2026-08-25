package md.ramaiana.foodmarket.domain.email.core.request;

import lombok.NonNull;

/**
 * Request to send a templated email.
 */
public record EmailSendRequest(
    @NonNull EmailRecipient recipient,
    @NonNull EmailTemplateVariables variables
) {
}
