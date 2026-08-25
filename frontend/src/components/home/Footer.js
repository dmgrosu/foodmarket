import React from "react"
import { Box, Typography, Container } from "@material-ui/core"
import {Link} from "react-router-dom";
import {useTranslation} from "react-i18next";

export default function Footer() {
    const {t} = useTranslation();
    return (
        <Box bgcolor="#2f3b52" color="white" py={6} mt={8}>
            <Container maxWidth="lg">
                <Box display="flex" alignItems="center" mb={1}>
                    <img
                        src="/logos/rama-dark.png"
                        alt="Ramaiana"
                        style={{ height: 50, marginRight: 16 }}
                    />
                    <Typography variant="h6">
                        Ramaiana On-line
                    </Typography>
                </Box>
                <Typography variant="body2" style={{ marginTop: 8 }}>
                    {t('home.footer.subTitle')}
                </Typography>
                <Typography variant="body2" style={{ marginTop: 16 }}>
                    {t('common.copyright')}
                    <Link color="inherit" to="/">
                        Ramaiana SRL
                    </Link>
                    {new Date().getFullYear()}
                </Typography>
            </Container>
        </Box>
    )
}