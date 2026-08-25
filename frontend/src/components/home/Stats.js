import React from "react"
import { Typography, Grid, Paper } from "@material-ui/core"
import {useTranslation} from "react-i18next";

const stats = [
    { value: "4000+", labelKey: "products" },
    { value: "250+", labelKey: "clients" },
    { value: "100+", labelKey: "suppliers" },
    { value: "20+", labelKey: "years" }
]

export default function Stats() {
    const {t} = useTranslation();
    return (
        <>
            <Typography variant="h4" gutterBottom>
                {t('home.stats.title')}
            </Typography>

            <Grid container spacing={3}>
                {stats.map((s, i) => (
                    <Grid item xs={6} md={3} key={i}>
                        <Paper style={{ padding: 24, textAlign: "center" }}>
                            <Typography variant="h4">
                                {s.value}
                            </Typography>

                            <Typography color="textSecondary">
                                {t(`home.stats.labels.${s.labelKey}`)}
                            </Typography>
                        </Paper>
                    </Grid>
                ))}
            </Grid>
        </>
    )
}
