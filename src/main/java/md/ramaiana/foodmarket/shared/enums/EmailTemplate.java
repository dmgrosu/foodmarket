package md.ramaiana.foodmarket.shared.enums;

import lombok.NonNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * Mailjet transactional email templates. Each constant carries one template id per {@link Language},
 * because the copy is translated in the Mailjet console rather than at send time — the source files
 * live in docs/mailjet/.
 */
public enum EmailTemplate {

    REGISTRATION_CONFIRMATION(Map.of(
        Language.RU, "8297951",
        Language.RO, "8298384",
        Language.EN, "8298386"
    )),

    ACCOUNT_ACTIVATED(Map.of(
        Language.RU, "8303532",
        Language.RO, "8303522",
        Language.EN, "8303536"
    ));

    private final Map<Language, String> idByLanguage;

    EmailTemplate(@NonNull Map<Language, String> idByLanguage) {
        for (Language language : Language.values()) {
            if (!idByLanguage.containsKey(language)) {
                // Fail at startup rather than at send time: adding a Language without importing the
                // matching template would otherwise only surface when that user registers.
                throw new IllegalStateException(
                    "Email template " + name() + " has no id for language " + language);
            }
        }
        this.idByLanguage = new EnumMap<>(idByLanguage);
    }

    /**
     * The Mailjet template id to send for a given language.
     */
    @NonNull
    public String getId(@NonNull Language language) {
        return idByLanguage.get(language);
    }
}
