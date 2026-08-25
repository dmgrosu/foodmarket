import i18n from "i18next";
import {initReactI18next} from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import ru from "./locales/ru.json";
import ro from "./locales/ro.json";
import en from "./locales/en.json";

// Order matters: a language the user has explicitly chosen (localStorage) always wins over
// the browser's Accept-Language-derived navigator.language. `fallbackLng` only kicks in when
// neither source matches one of our three locales.
i18n
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
        resources: {
            ru: {translation: ru},
            ro: {translation: ro},
            en: {translation: en},
        },
        fallbackLng: "ru",
        supportedLngs: ["ru", "ro", "en"],
        nonExplicitSupportedLngs: true,
        detection: {
            order: ["localStorage", "navigator"],
            caches: ["localStorage"],
        },
        interpolation: {
            // React already escapes interpolated values.
            escapeValue: false,
        },
    });

// Keep the <html lang> attribute in sync so screen readers and browser spellcheck follow the
// active language, not the hardcoded "en" from public/index.html.
document.documentElement.lang = i18n.resolvedLanguage || i18n.language;
i18n.on("languageChanged", (lng) => {
    document.documentElement.lang = lng;
});

export default i18n;
