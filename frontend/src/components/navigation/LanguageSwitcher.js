import React from 'react';
import {MenuItem, Select} from "@material-ui/core";
import {useTranslation} from "react-i18next";

const LANGUAGES = ["ru", "ro", "en"];

const LanguageSwitcher = () => {

    const {t, i18n} = useTranslation();

    return (
        <Select value={i18n.resolvedLanguage || i18n.language}
                onChange={(e) => i18n.changeLanguage(e.target.value)}
                disableUnderline
                style={{color: "inherit", marginRight: 8}}
        >
            {LANGUAGES.map(lng => (
                <MenuItem key={lng} value={lng}>{t(`language.${lng}`)}</MenuItem>
            ))}
        </Select>
    );
};

export default LanguageSwitcher;
