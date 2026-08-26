package md.ramaiana.foodmarket.shared.enums;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class EmailTemplateTest {

    @ParameterizedTest
    @EnumSource(EmailTemplate.class)
    void every_template_should_have_an_id_for_every_language(EmailTemplate template) {
        // The enum constructor enforces this, so a missing id fails at startup rather than at send
        // time. This test is what makes that failure obvious instead of an ExceptionInInitializerError.
        for (Language language : Language.values()) {
            assertThat(template.getId(language))
                .as("%s id for %s", template, language)
                .isNotNull()
                .isNotBlank();
        }
    }

    @ParameterizedTest
    @EnumSource(EmailTemplate.class)
    void every_template_id_should_be_numeric(EmailTemplate template) {
        // MailjetAdapter does Long.parseLong on these; a typo would only surface on a real send.
        for (Language language : Language.values()) {
            assertThat(template.getId(language))
                .as("%s id for %s", template, language)
                .containsOnlyDigits();
        }
    }

    @Test
    void registration_confirmation_should_point_at_the_imported_russian_template() {
        // RO and EN are still the "0000000" placeholder until those templates are imported. A
        // placeholder id is not a crash: Mailjet rejects it, RegistrationConfirmationMailUseCase
        // swallows the MailException, and the caller sees confirmationEmailSent=false.
        assertThat(EmailTemplate.REGISTRATION_CONFIRMATION.getId(Language.RU)).isEqualTo("8297951");
    }
}
