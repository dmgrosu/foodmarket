package md.ramaiana.foodmarket.config;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for registration email confirmation.
 * Binds from application.yml under the "registration" prefix.
 */
@ConfigurationProperties(prefix = "registration")
public record RegistrationProperties(

    /**
     * Frontend page the confirmation link points at, without the token — the token is appended as a
     * confirmationToken query parameter when the link is built.
     */
    @NonNull String confirmationPageUrl,

    /**
     * How long a confirmation link stays usable after it is issued.
     */
    int confirmationLinkValidityHours,

    /**
     * How long a user must wait between two confirmation emails.
     */
    int resendCooldownSeconds
) {

    public RegistrationProperties {
        if (confirmationPageUrl.isBlank()) {
            throw new IllegalStateException("registration.confirmation-page-url must be configured");
        }
        if (confirmationLinkValidityHours <= 0) {
            throw new IllegalStateException("registration.confirmation-link-validity-hours must be positive");
        }
        if (resendCooldownSeconds < 0) {
            throw new IllegalStateException("registration.resend-cooldown-seconds must not be negative");
        }
    }
}
