import React from "react"
import { Grid, Typography, Button, Box, Container } from "@material-ui/core"
import {Link} from "react-router-dom";
import {useTranslation} from "react-i18next";

export default function Hero() {
    const {t} = useTranslation();
    return (
        <Box py={10}>
            <Container maxWidth="lg">
                <Grid container spacing={6} alignItems="center">

                    <Grid item xs={12} md={6}>
                        <Typography variant="h3" gutterBottom>
                            {t('home.hero.title')}
                        </Typography>

                        <Typography variant="h6" color="textSecondary">
                            {t('home.hero.description')}
                        </Typography>

                        <Box mt={4}>
                            <Button
                                variant="contained"
                                color="primary"
                                size="large"
                                style={{ marginRight: 16 }}
                                component={Link} to="/signIn"
                            >
                                {t('home.hero.login')}
                            </Button>

                            <Button
                                variant="outlined"
                                color="primary"
                                size="large"
                                component={Link} to="/signUp"
                            >
                                {t('home.hero.register')}
                            </Button>
                        </Box>
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <img
                            src="/images/food-distribution.jpg"
                            alt="food distribution"
                            style={{
                                width: "100%",
                                borderRadius: 12,
                                boxShadow: "0 10px 30px rgba(0,0,0,0.15)"
                            }}
                        />
                    </Grid>

                </Grid>
            </Container>
        </Box>
    )
}