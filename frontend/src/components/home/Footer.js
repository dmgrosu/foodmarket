import React from "react"
import { Box, Typography, Container } from "@material-ui/core"
import {Link} from "react-router-dom";
import {footerContent} from "../../data/homepageContent";

export default function Footer() {
    return (
        <Box bgcolor="#2f3b52" color="white" py={6} mt={8}>
            <Container maxWidth="lg">

                <Typography variant="h6">
                    {footerContent.title}
                </Typography>

                <Typography variant="body2" style={{ marginTop: 8 }}>
                    {footerContent.subTitle}
                </Typography>

                <Typography variant="body2" style={{ marginTop: 16 }}>
                    Copyright ©
                    <Link color="inherit" to="/">
                        Ramaiana SRL
                    </Link>
                    {new Date().getFullYear()}
                </Typography>

            </Container>
        </Box>
    )
}