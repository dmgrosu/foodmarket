package md.ramaiana.foodmarket.shared.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Mailjet transactional email templates.
 * Each constant carries the template id as shown in the Mailjet console.
 */
@Getter
@RequiredArgsConstructor
public enum EmailTemplate {

    LOGIN_CONFIRMATION("0000000");

    private final String id;
}
