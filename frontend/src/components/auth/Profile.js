import React from 'react';
import {Typography} from "@material-ui/core";
import {useTranslation} from "react-i18next";

const Profile = () => {
    const {t} = useTranslation();
    return (
        <div>
            <Typography>
                {t('profile.placeholder')}
            </Typography>
        </div>
    )
}

export default Profile;
