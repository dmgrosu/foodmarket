package md.ramaiana.foodmarket.domain.email.core.request;

import lombok.NonNull;
import md.ramaiana.foodmarket.shared.enums.EmailTemplate;
import md.ramaiana.foodmarket.shared.enums.Language;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Account activated email template variables: where to sign in. The language selects which of the
 * three imported templates is sent, so it is not part of the variables payload.
 */
public record AccountActivatedVariables(
    @NonNull String loginUrl,
    @NonNull Language language
) implements EmailTemplateVariables {

    public AccountActivatedVariables {
        if (loginUrl.isBlank()) {
            throw new IllegalArgumentException("Login URL cannot be blank");
        }
    }

    @Override
    public EmailTemplate template() {
        return EmailTemplate.ACCOUNT_ACTIVATED;
    }

    @Override
    public Map<String, Object> variables() {
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("login_url", loginUrl);
        return Collections.unmodifiableMap(vars);
    }
}
