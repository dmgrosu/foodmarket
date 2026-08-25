import React from "react"
import { Grid, Typography, Box } from "@material-ui/core"
import {useTranslation} from "react-i18next";
import {partnerLogos} from "../../data/homepageContent";

export default function Partners() {
    const {t} = useTranslation();

    return (
        <Box py={8} textAlign="center">

            <Typography variant="h4" gutterBottom>
                {t('home.partners.title')}
            </Typography>

            <Typography color="textSecondary" gutterBottom>
                {t('home.partners.subTitle')}
            </Typography>

            <Grid
                container
                spacing={4}
                justify="center"
                style={{ marginTop: 20 }}
            >
                {partnerLogos.map((logo, i) => (
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