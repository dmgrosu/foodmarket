import React from "react"
import {Typography, Paper} from "@material-ui/core"
import {useTranslation} from "react-i18next";

const PARAGRAPH_KEYS = ["p1", "p2", "p3"];

export default function About() {
    const {t} = useTranslation();
    return (
        <>
            <Typography variant="h4" gutterBottom>
                {t('home.about.title')}
            </Typography>

            <Paper style={{padding: 24}}>
                {PARAGRAPH_KEYS.map((key) => (
                    <Typography paragraph key={key}>
                        {t(`home.about.paragraphs.${key}`)}
                    </Typography>
                ))}
            </Paper>
        </>
    )
}
