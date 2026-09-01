package md.ramaiana.foodmarket.domain.email.core.request;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.NonNull;
import md.ramaiana.foodmarket.shared.enums.EmailTemplate;
import md.ramaiana.foodmarket.shared.enums.Language;

/**
 * Password reset email template variables: reset link and expiry. The language selects which of the
 * three imported templates is sent, so it is not part of the variables payload.
 */
public record PasswordResetVariables(
    @NonNull String resetUrl,
    int expiresInHours,
    @NonNull Language language
) implements EmailTemplateVariables {

    public PasswordResetVariables {
        if (resetUrl.isBlank()) {
            throw new IllegalArgumentException("Reset URL cannot be blank");
        }
        if (expiresInHours <= 0) {
            throw new IllegalArgumentException("Expiry must be positive");
        }
    }

    @Override
    public EmailTemplate template() {
        return EmailTemplate.PASSWORD_RESET;
    }

    @Override
    public Map<String, Object> variables() {
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("reset_url", resetUrl);
        vars.put("expires_in_hours", expiresInHours);
        return Collections.unmodifiableMap(vars);
    }
}
