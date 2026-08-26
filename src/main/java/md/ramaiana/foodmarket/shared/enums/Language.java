package md.ramaiana.foodmarket.shared.enums;

import lombok.Getter;
import lombok.NonNull;

/**
 * Languages the application is translated into, mirroring frontend/src/i18n (supportedLngs).
 * Persisted per user so any email we send later — a confirmation, a resend triggered by an
 * administrator, an activation notice — reaches them in their own language rather than in the
 * language of whoever happened to trigger it.
 */
@Getter
public enum Language {

  RU("ru"),
  RO("ro"),
  EN("en");

  /**
   * The tag the frontend uses, matching i18next's resolvedLanguage.
   */
  private final String tag;

  Language(@NonNull String tag) {
    this.tag = tag;
  }

  /**
   * Resolve a language tag coming from the browser. Unknown, malformed and missing tags fall back to
   * {@link #RU}, the same fallbackLng the frontend uses, so a user is never left without copy.
   */
  @NonNull
  public static Language fromTag(String tag) {
    if (tag == null || tag.isBlank()) {
      return RU;
    }
    // Accept regional tags such as "ro-MD" or "en-US" by matching on the primary subtag only.
    String primarySubtag = tag.split("-")[0].trim().toLowerCase();
    for (Language language : values()) {
      if (language.tag.equals(primarySubtag)) {
        return language;
      }
    }
    return RU;
  }
}
