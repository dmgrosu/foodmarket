import React from 'react';
import {useDispatch, useSelector} from 'react-redux';
import {useTranslation} from 'react-i18next';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';
import {CircularProgress} from '@material-ui/core';
import {resendConfirmation} from '../../store/actions/authActions';

/**
 * The "we sent you a confirmation link" panel. Not a route: SignUp swaps it in place of the form
 * once registration succeeds, so the user stays on /signUp and a refresh does not strand them on a
 * page with no registration behind it.
 */
const CheckEmail = () => {
    const {t} = useTranslation();
    const dispatch = useDispatch();
    const {signUpEmail, confirmationEmailSent, isLoading} = useSelector(state => state.authReducer);

    return (
        <Box display="flex" flexDirection="column" alignItems="center" textAlign="center">
            <Typography component="h1" variant="h5" gutterBottom>
                {t('auth.checkEmail.title')}
            </Typography>
            <Typography>
                {confirmationEmailSent
                    ? t('auth.checkEmail.sent', {email: signUpEmail})
                    : t('auth.checkEmail.notSent', {email: signUpEmail})}
            </Typography>
            <Box mt={3}>
                <Button
                    variant="contained"
                    color="primary"
                    disabled={isLoading}
                    onClick={() => dispatch(resendConfirmation(signUpEmail))}
                >
                    {t('auth.checkEmail.resend')}
                </Button>
                {isLoading && <CircularProgress size={24}/>}
            </Box>
        </Box>
    );
};

export default CheckEmail;
