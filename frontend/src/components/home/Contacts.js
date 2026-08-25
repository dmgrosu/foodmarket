import React from "react"
import {
    Typography,
    Grid,
    Paper,
    Button
} from "@material-ui/core"
import {contacts} from "../../data/homepageContent";
import {Link} from "react-router-dom";
import {useTranslation} from "react-i18next";

const WAREHOUSE_KEYS = ["w1", "w2", "w3", "w4", "w5"];

export default function Contacts() {
    const {t} = useTranslation();
    return (
        <>
            <Typography variant="h4" gutterBottom>
                {t('home.contacts.title')}
            </Typography>

            <Grid container spacing={3}>
                {contacts.cards.map((card, i) => {
                    const key = WAREHOUSE_KEYS[i];
                    return (
                        <Grid item xs={12} md={6} key={key}>
                            <Paper style={{ padding: 24 }}>
                                <Typography variant="h6">
                                    {t('home.contacts.warehouseTitle', {n: i + 1})}
                                </Typography>

                                <Typography>
                                    {t('home.contacts.labels.phone')} {card.phone}
                                </Typography>

                                <Typography>
                                    {t('home.contacts.labels.email')} {card.email}
                                </Typography>

                                <Typography>
                                    {t('home.contacts.labels.address')} {t(`home.contacts.warehouses.${key}.address`)}
                                </Typography>
                            </Paper>
                        </Grid>
                    );
                })}

                <Grid item xs={12} md={6}>
                    <Paper style={{ padding: 24 }}>
                        <Typography variant="h6" gutterBottom>
                            {t('home.contacts.getAccess.title')}
                        </Typography>

                        <Typography paragraph>
                            {t('home.contacts.getAccess.text')}
                        </Typography>

                        <Button
                            variant="contained"
                            color="primary"
                            size="large"
                            component={Link} to="/signUp"
                        >
                            {t('home.contacts.getAccess.button')}
                        </Button>
                    </Paper>
                </Grid>
            </Grid>
        </>
    )
}
