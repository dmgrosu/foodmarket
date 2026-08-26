package md.ramaiana.foodmarket.shared.enums;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class LanguageTest {

    @Test
    void fromTag_should_resolve_each_supported_tag() {
        assertThat(Language.fromTag("ru")).isEqualTo(Language.RU);
        assertThat(Language.fromTag("ro")).isEqualTo(Language.RO);
        assertThat(Language.fromTag("en")).isEqualTo(Language.EN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ro-MD", "en-US", "RO", "  en  "})
    void fromTag_should_accept_regional_and_untidy_tags(String tag) {
        assertThat(Language.fromTag(tag)).isNotEqualTo(Language.RU);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "de", "zz-ZZ", "not-a-tag"})
    void fromTag_should_fall_back_to_russian(String tag) {
        // Mirrors the frontend's fallbackLng, so a user is never left without copy.
        assertThat(Language.fromTag(tag)).isEqualTo(Language.RU);
    }

    @Test
    void tags_should_match_the_frontend_supported_languages() {
        assertThat(Language.values()).extracting(Language::getTag)
            .containsExactlyInAnyOrder("ru", "ro", "en");
    }
}
