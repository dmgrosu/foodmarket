package md.ramaiana.foodmarket.domain.email.core.request;

import md.ramaiana.foodmarket.shared.enums.EmailTemplate;
import md.ramaiana.foodmarket.shared.enums.Language;

import java.util.Map;

/**
 * Sealed interface for template-specific variables.
 * Ensures type safety: a template can only be sent with the correct variable set.
 */
public sealed interface EmailTemplateVariables permits RegistrationConfirmationVariables, AccountActivatedVariables {
    /**
     * Which template these variables belong to.
     */
    EmailTemplate template();

    /**
     * Which language the recipient should be written to. Selects the template id, so the copy itself
     * is never branched at send time.
     */
    Language language();

    /**
     * The Mailjet template "Variables" payload — a flat map of key-value pairs
     * that will be rendered into the template.
     */
    Map<String, Object> variables();
}
