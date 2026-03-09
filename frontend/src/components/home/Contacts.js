import React from "react"
import {
    Typography,
    Grid,
    Paper,
    Button
} from "@material-ui/core"
import {contacts} from "../../data/homepageContent";
import {Link} from "react-router-dom";

export default function Contacts() {
    return (
        <>
            <Typography variant="h4" gutterBottom>
                {contacts.title}
            </Typography>

            <Grid container spacing={3}>
                {contacts.cards.map((card) => (
                    <Grid item xs={12} md={6}>
                        <Paper style={{ padding: 24 }}>
                            <Typography variant="h6">
                                {card.title}
                            </Typography>

                            <Typography>
                                {card.phone}
                            </Typography>

                            <Typography>
                                {card.email}
                            </Typography>

                            <Typography>
                                {card.address}
                            </Typography>
                        </Paper>
                    </Grid>
                ))}

                <Grid item xs={12} md={6}>
                    <Paper style={{ padding: 24 }}>
                        <Typography variant="h6" gutterBottom>
                            {contacts.getAccess.title}
                        </Typography>

                        <Typography paragraph>
                            {contacts.getAccess.text}
                        </Typography>

                        <Button
                            variant="contained"
                            color="primary"
                            size="large"
                            component={Link} to="/signUp"
                        >
                            {contacts.getAccess.button}
                        </Button>
                    </Paper>
                </Grid>
            </Grid>
        </>
    )
}