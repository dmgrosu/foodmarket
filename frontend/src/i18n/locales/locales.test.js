import ru from "./ru.json";
import ro from "./ro.json";
import en from "./en.json";

// The risk in an i18n setup isn't day-one correctness, it's drift: someone adds a key to one
// file and forgets the other two. This test catches that in CI instead of a raw "admin.title"
// key rendering in production.

const flatten = (obj, prefix = "") =>
    Object.entries(obj).reduce((keys, [key, value]) => {
        const path = prefix ? `${prefix}.${key}` : key;
        if (value !== null && typeof value === "object" && !Array.isArray(value)) {
            return keys.concat(flatten(value, path));
        }
        return keys.concat([{path, value}]);
    }, []);

const LOCALES = {ru, ro, en};

describe("locale key parity", () => {
    const flattened = Object.fromEntries(
        Object.entries(LOCALES).map(([lng, resource]) => [lng, flatten(resource)])
    );
    const keySets = Object.fromEntries(
        Object.entries(flattened).map(([lng, entries]) => [lng, new Set(entries.map(e => e.path))])
    );

    test.each(["ro", "en"])("%s has exactly the same keys as ru", (lng) => {
        const ruKeys = keySets.ru;
        const otherKeys = keySets[lng];

        const missing = [...ruKeys].filter(k => !otherKeys.has(k));
        const extra = [...otherKeys].filter(k => !ruKeys.has(k));

        expect({missing, extra}).toEqual({missing: [], extra: []});
    });

    test.each(Object.keys(LOCALES))("%s has no empty or whitespace-only values", (lng) => {
        const blank = flattened[lng]
            .filter(({value}) => typeof value !== "string" || value.trim() === "")
            .map(({path}) => path);

        expect(blank).toEqual([]);
    });
});
