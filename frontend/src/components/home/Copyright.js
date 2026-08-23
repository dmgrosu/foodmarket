import React from 'react';
import Typography from "@material-ui/core/Typography";
import {Link} from "react-router-dom";
import {useTranslation} from "react-i18next";

const Copyright = () => {
    const {t} = useTranslation();
    return (
        <Typography variant="body2" color="textSecondary" align="center">
            {t('common.copyright')}{' '}
            <Link color="inherit" to="/">
                Ramaiana
            </Link>{' '}
            {new Date().getFullYear()}
        </Typography>
    );
}

export default Copyright;
