import React from "react"
import {
    Typography,
    Grid,
    Card,
    CardContent
} from "@material-ui/core"
import {useTranslation} from "react-i18next";

const CARD_KEYS = ["retail", "restaurants", "hotels", "wholesale"];

export default function TargetAudience() {
    const {t} = useTranslation();
    return (
        <>
            <Typography variant="h4" gutterBottom>
                {t('home.audience.title')}
            </Typography>

            <Grid container spacing={3}>
                {CARD_KEYS.map((key) => (
                    <Grid item xs={12} sm={6} md={3} key={key}>
                        <Card>
                            <CardContent>
                                <Typography variant="h6">
                                    {t(`home.audience.cards.${key}.title`)}
                                </Typography>

                                <Typography color="textSecondary">
                                    {t(`home.audience.cards.${key}.text`)}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </>
    )
}
