package md.ramaiana.foodmarket.domain.email.core.request;

import lombok.NonNull;
import md.ramaiana.foodmarket.shared.enums.EmailTemplate;
import md.ramaiana.foodmarket.shared.enums.Language;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registration confirmation email template variables: magic link and expiry. The language selects
 * which of the three imported templates is sent, so it is not part of the variables payload.
 */
public record RegistrationConfirmationVariables(
    @NonNull String confirmationUrl,
    int expiresInHours,
    @NonNull Language language
) implements EmailTemplateVariables {

    public RegistrationConfirmationVariables {
        if (confirmationUrl.isBlank()) {
            throw new IllegalArgumentException("Confirmation URL cannot be blank");
        }
        if (expiresInHours <= 0) {
            throw new IllegalArgumentException("Expiry must be positive");
        }
    }

    @Override
    public EmailTemplate template() {
        return EmailTemplate.REGISTRATION_CONFIRMATION;
    }

    @Override
    public Map<String, Object> variables() {
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("confirmation_url", confirmationUrl);
        vars.put("expires_in_hours", expiresInHours);
        return Collections.unmodifiableMap(vars);
    }
}
