package md.ramaiana.foodmarket.config;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Mailjet integration.
 * Binds from application.yml under the "mailjet" prefix.
 * Template ids are not configured here — each EmailTemplate constant carries its own id.
 */
@ConfigurationProperties(prefix = "mailjet")
public record MailjetProperties(
    boolean enabled,
    @NonNull String baseUrl,
    String apiKey,
    String secretKey,
    int connectTimeoutMs,
    int readTimeoutMs,
    @NonNull Sender sender
) {

    public MailjetProperties {
        if (enabled) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("mailjet.api-key must be configured when mailjet.enabled=true");
            }
            if (secretKey == null || secretKey.isBlank()) {
                throw new IllegalStateException("mailjet.secret-key must be configured when mailjet.enabled=true");
            }
            if (sender.email() == null || sender.email().isBlank()) {
                throw new IllegalStateException("mailjet.sender.email must be configured when mailjet.enabled=true");
            }
        }
    }

    /**
     * Sender identity for all outgoing emails.
     */
    public record Sender(
        String email,
        String name
    ) {
    }
}
