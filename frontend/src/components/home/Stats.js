import React from "react"
import { Typography, Grid, Paper } from "@material-ui/core"

const stats = [
    { value: "4000+", label: "товаров" },
    { value: "250+", label: "клиентов" },
    { value: "100+", label: "поставщиков" },
    { value: "20+", label: "лет на рынке" }
]

export default function Stats() {
    return (
        <>
            <Typography variant="h4" gutterBottom>
                Наша статистика
            </Typography>

            <Grid container spacing={3}>
                {stats.map((s, i) => (
                    <Grid item xs={6} md={3} key={i}>
                        <Paper style={{ padding: 24, textAlign: "center" }}>
                            <Typography variant="h4">
                                {s.value}
                            </Typography>

                            <Typography color="textSecondary">
                                {s.label}
                            </Typography>
                        </Paper>
                    </Grid>
                ))}
            </Grid>
        </>
    )
}