import React from "react"
import {
    Typography,
    Grid,
    Card,
    CardContent
} from "@material-ui/core"
import {advantages} from "../../data/homepageContent";

export default function Advantages() {
    return (
        <>
            <Typography variant="h4" gutterBottom>
                {advantages.title}
            </Typography>

            <Grid container spacing={3}>
                {advantages.cards.map((item, i) => (
                    <Grid item xs={12} sm={6} md={4} key={i}>
                        <Card>
                            <CardContent>
                                <Typography>{item}</Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </>
    )
}