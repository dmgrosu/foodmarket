import React from "react"
import {
    Typography,
    Grid,
    Card,
    CardContent
} from "@material-ui/core"
import {howItWorks} from "../../data/homepageContent";

export default function HowItWorks() {
    return (
        <>
            <Typography variant="h4" gutterBottom>
                {howItWorks.title}
            </Typography>

            <Grid container spacing={3}>
                {howItWorks.steps.map((step, i) => (
                    <Grid item xs={12} md={4} key={i}>
                        <Card>
                            <CardContent>
                                <Typography variant="h6">
                                    {i + 1}. {step.title}
                                </Typography>

                                <Typography color="textSecondary">
                                    {step.text}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </>
    )
}