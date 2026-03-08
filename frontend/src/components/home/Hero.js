import React from "react"
import { Grid, Typography, Button, Box, Container } from "@material-ui/core"
import {Link} from "react-router-dom";
import {heroContent} from "../../data/homepageContent";

export default function Hero() {
    return (
        <Box py={10}>
            <Container maxWidth="lg">
                <Grid container spacing={6} alignItems="center">

                    <Grid item xs={12} md={6}>
                        <Typography variant="h3" gutterBottom>
                            {heroContent.title}
                        </Typography>

                        <Typography variant="h6" color="textSecondary">
                            {heroContent.description}
                        </Typography>

                        <Box mt={4}>
                            <Button
                                variant="contained"
                                color="primary"
                                size="large"
                                style={{ marginRight: 16 }}
                                component={Link} to="/signIn"
                            >
                                Войти
                            </Button>

                            <Button
                                variant="outlined"
                                color="primary"
                                size="large"
                                component={Link} to="/signUp"
                            >
                                Регистрация
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