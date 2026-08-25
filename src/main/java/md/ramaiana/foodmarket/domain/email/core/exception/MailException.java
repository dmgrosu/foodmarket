package md.ramaiana.foodmarket.domain.email.core.exception;

import lombok.NonNull;

/**
 * Exception thrown when email sending to Mailjet fails.
 * Falls through to ControllerAdviceConfig#handle(Exception) → 500 with error id.
 */
public class MailException extends RuntimeException {

    /**
     * Constructor with message.
     */
    public MailException(@NonNull String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     */
    public MailException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }
}
