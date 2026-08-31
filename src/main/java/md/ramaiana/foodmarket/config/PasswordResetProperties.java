package md.ramaiana.foodmarket.config;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for password reset links.
 * Binds from application.yml under the "password-reset" prefix.
 */
@ConfigurationProperties(prefix = "password-reset")
public record PasswordResetProperties(

    /**
     * Frontend page the reset link points at, without the token — the token is appended as a
     * resetToken query parameter when the link is built.
     */
    @NonNull String resetPageUrl,

    /**
     * How long a reset link stays usable after it is issued. Deliberately much shorter than the
     * registration confirmation window: this link changes a credential.
     */
    int linkValidityHours,

    /**
     * How long a user must wait between two reset emails.
     */
    int requestCooldownSeconds
) {

  public PasswordResetProperties {
    if (resetPageUrl.isBlank()) {
      throw new IllegalStateException("password-reset.reset-page-url must be configured");
    }
    if (linkValidityHours <= 0) {
      throw new IllegalStateException("password-reset.link-validity-hours must be positive");
    }
    if (requestCooldownSeconds < 0) {
      throw new IllegalStateException("password-reset.request-cooldown-seconds must not be negative");
    }
  }
}
