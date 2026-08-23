import React from "react"
import {
    Typography,
    Grid,
    Card,
    CardContent
} from "@material-ui/core"
import {useTranslation} from "react-i18next";

const STEP_KEYS = ["registration", "catalogAccess", "orderPlacement"];

export default function HowItWorks() {
    const {t} = useTranslation();
    return (
        <>
            <Typography variant="h4" gutterBottom>
                {t('home.howItWorks.title')}
            </Typography>

            <Grid container spacing={3}>
                {STEP_KEYS.map((key, i) => (
                    <Grid item xs={12} md={4} key={key}>
                        <Card>
                            <CardContent>
                                <Typography variant="h6">
                                    {i + 1}. {t(`home.howItWorks.steps.${key}.title`)}
                                </Typography>

                                <Typography color="textSecondary">
                                    {t(`home.howItWorks.steps.${key}.text`)}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </>
    )
}
