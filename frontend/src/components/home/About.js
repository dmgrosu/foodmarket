import React from "react"
import {Typography, Paper} from "@material-ui/core"
import {about} from "../../data/homepageContent";

export default function About() {
    return (
        <>
            <Typography variant="h4" gutterBottom>
                {about.title}
            </Typography>

            <Paper style={{padding: 24}}>
                {about.paragraphs.map((step) => (
                    <Typography paragraph>
                        {step}
                    </Typography>
                ))}
            </Paper>
        </>
    )
}