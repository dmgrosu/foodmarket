import React from "react"
import {
    Typography,
    Grid,
    Card,
    CardContent
} from "@material-ui/core"
import {useTranslation} from "react-i18next";

const CARD_KEYS = ["supply", "assortment", "prices", "management", "suppliers", "manager"];

export default function Advantages() {
    const {t} = useTranslation();
    return (
        <>
            <Typography variant="h4" gutterBottom>
                {t('home.advantages.title')}
            </Typography>

            <Grid container spacing={3}>
                {CARD_KEYS.map((key) => (
                    <Grid item xs={12} sm={6} md={4} key={key}>
                        <Card>
                            <CardContent>
                                <Typography>{t(`home.advantages.cards.${key}`)}</Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </>
    )
}
