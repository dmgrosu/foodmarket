package md.ramaiana.foodmarket.domain.email.core.request;

import lombok.NonNull;
import md.ramaiana.foodmarket.shared.enums.EmailTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Login confirmation email template variables: magic link + expiry.
 */
public record LoginConfirmationVariables(
    @NonNull String confirmationUrl,
    int expiresInMinutes
) implements EmailTemplateVariables {

    public LoginConfirmationVariables {
        if (confirmationUrl.isBlank()) {
            throw new IllegalArgumentException("Confirmation URL cannot be blank");
        }
        if (expiresInMinutes <= 0) {
            throw new IllegalArgumentException("Expiry must be positive");
        }
    }

    @Override
    public EmailTemplate template() {
        return EmailTemplate.LOGIN_CONFIRMATION;
    }

    @Override
    public Map<String, Object> variables() {
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("confirmation_url", confirmationUrl);
        vars.put("expires_in_minutes", expiresInMinutes);
        return Collections.unmodifiableMap(vars);
    }
}
