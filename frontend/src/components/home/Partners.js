import React from "react"
import { Grid, Typography, Box } from "@material-ui/core"
import {partners} from "../../data/homepageContent";

export default function Partners() {

    return (
        <Box py={8} textAlign="center">

            <Typography variant="h4" gutterBottom>
                {partners.title}
            </Typography>

            <Typography color="textSecondary" gutterBottom>
                {partners.subTitle}
            </Typography>

            <Grid
                container
                spacing={4}
                justify="center"
                style={{ marginTop: 20 }}
            >
                {partners.logos.map((logo, i) => (
                    <Grid item key={i}>
                        <img
                            src={logo}
                            alt="partner"
                            style={{
                                height: 40,
                                opacity: 0.7
                            }}
                        />
                    </Grid>
                ))}
            </Grid>

        </Box>
    )
}