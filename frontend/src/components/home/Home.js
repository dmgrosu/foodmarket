import React from "react"
import { Box, Container } from "@material-ui/core"

import Hero from "./Hero"
import TargetAudience from "./TargetAudience"
import HowItWorks from "./HowItWorks"
import Advantages from "./Advantages"
import Partners from "./Partners"
import About from "./About"
import Contacts from "./Contacts"
import Footer from "./Footer"

export default function HomePage() {
    return (
        <>
            <Hero />

            <Box bgcolor="#f7f9fc" py={8}>
                <Container maxWidth="lg">
                    <TargetAudience />
                </Container>
            </Box>

            <Container maxWidth="lg">
                <HowItWorks />
            </Container>

            <Box bgcolor="#f7f9fc" py={8}>
                <Container maxWidth="lg">
                    <Advantages />
                </Container>
            </Box>

            <Container maxWidth="lg">
                <Partners />
            </Container>

            <Box bgcolor="#f7f9fc" py={8}>
                <Container maxWidth="md">
                    <About />
                </Container>
            </Box>

            <Container maxWidth="lg">
                <Contacts />
            </Container>

            <Footer />
        </>
    )
}