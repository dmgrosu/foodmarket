import React from 'react';
import {render, screen, within} from '@testing-library/react';
import {BrowserRouter} from "react-router-dom";
import {Provider} from "react-redux";
import {applyMiddleware, createStore} from "redux";
import thunk from "redux-thunk";
import App from './App';
import {rootReducer} from "./store/reducers";
import i18n from "./i18n";
import ru from "./i18n/locales/ru.json";

// Smoke test over the real provider stack, mirroring index.js. Deliberately shallow on
// assertions and broad on wiring: it catches a broken reducer shape, a bad route table, a
// missing translation key and an unrenderable component tree — failures that would otherwise
// only surface in the browser.

const renderApp = () => render(
    <Provider store={createStore(rootReducer, applyMiddleware(thunk))}>
        <BrowserRouter>
            <App/>
        </BrowserRouter>
    </Provider>
);

// i18next renders a missing key as the key itself. Text nodes are examined one at a time
// rather than via container.textContent, because that concatenates siblings without a
// separator ("Englishhome.hero.title") and destroys the word boundaries a regex needs.
const RAW_KEY_PATTERN = new RegExp(`^(${Object.keys(ru).join("|")})(\\.[A-Za-z0-9_]+)+$`);

const rawTranslationKeysIn = (root) => {
    const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
    const found = [];
    while (walker.nextNode()) {
        const text = walker.currentNode.textContent.trim();
        if (RAW_KEY_PATTERN.test(text)) {
            found.push(text);
        }
    }
    return found;
};

describe("App", () => {

    beforeEach(async () => {
        // Pin the language so assertions do not depend on the runner's navigator.language.
        await i18n.changeLanguage("en");
    });

    test("renders the application shell", () => {
        renderApp();

        // The brand also appears in the footer, so scope the lookup to the navbar.
        expect(within(screen.getByRole("banner")).getByText("Ramaiana SRL")).toBeInTheDocument();
    });

    test("renders the home hero in the active language", () => {
        renderApp();

        // Matched on the hero's own heading: the same copy is reused as home.footer.subTitle,
        // so a plain text search would pass even with the hero broken.
        const headings = screen.getAllByRole("heading", {level: 3}).map(h => h.textContent);

        expect(headings).toContain(i18n.t("home.hero.title"));
    });

    test("shows no raw translation keys", () => {
        const {container} = renderApp();

        expect(rawTranslationKeysIn(container)).toEqual([]);
    });
});
