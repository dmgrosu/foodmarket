import React from "react"
import {
    Typography,
    Grid,
    Card,
    CardContent
} from "@material-ui/core"
import {audience} from "../../data/homepageContent";

export default function TargetAudience() {
    return (
        <>
            <Typography variant="h4" gutterBottom>
                {audience.title}
            </Typography>

            <Grid container spacing={3}>
                {audience.cards.map((item, i) => (
                    <Grid item xs={12} sm={6} md={3} key={i}>
                        <Card>
                            <CardContent>
                                <Typography variant="h6">
                                    {item.title}
                                </Typography>

                                <Typography color="textSecondary">
                                    {item.text}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </>
    )
}