import React, {useEffect, useState} from 'react';
import {Redirect, useLocation} from 'react-router-dom';
import {useDispatch, useSelector} from 'react-redux';
import {useTranslation} from 'react-i18next';
import Container from '@material-ui/core/Container';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Box from '@material-ui/core/Box';
import {CircularProgress} from '@material-ui/core';
import {confirmEmail, resendConfirmation} from '../../store/actions/authActions';

/**
 * Landing page for the magic link in the confirmation email: ?confirmationToken=... .
 * Confirmation is idempotent server-side, so a stale/expired/unknown token and a replayed link
 * that is no longer PENDING_CONFIRMATION both collapse into the same localized "invalid" state —
 * this page never tries to distinguish them, and offers a resend instead.
 */
const ConfirmEmail = () => {
    const {t} = useTranslation();
    const dispatch = useDispatch();
    const location = useLocation();
    const {isConfirming, confirmed, confirmError, isLoading, token} = useSelector(state => state.authReducer);
    const [resendEmail, setResendEmail] = useState('');

    const confirmationToken = new URLSearchParams(location.search).get('confirmationToken');
    const hasToken = Boolean(confirmationToken);

    useEffect(() => {
        if (hasToken) {
            dispatch(confirmEmail(confirmationToken));
        }
        // Only run once, on mount — the token in the URL does not change during this page's lifetime.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const isInvalid = !hasToken || confirmError;
    const isPending = hasToken && (isConfirming || (!confirmed && !confirmError));

    if (confirmed && token) {
        // Approved before the link was opened, so confirmEmail signed them straight in.
        return <Redirect to="/products"/>;
    }

    return (
        <Container component="main" maxWidth="xs">
            <Box mt={4} display="flex" flexDirection="column" alignItems="center" textAlign="center">
                <Typography component="h1" variant="h5" gutterBottom>
                    {t('auth.confirmEmail.title')}
                </Typography>

                {isPending && (
                    <Box display="flex" flexDirection="column" alignItems="center">
                        <CircularProgress/>
                        <Typography>{t('auth.confirmEmail.confirming')}</Typography>
                    </Box>
                )}

                {!isPending && confirmed && (
                    <Typography>{t('auth.confirmEmail.success')}</Typography>
                )}

                {!isPending && isInvalid && (
                    <>
                        <Typography color="error" gutterBottom>
                            {t('auth.confirmEmail.invalid')}
                        </Typography>
                        <Typography variant="subtitle2" gutterBottom>
                            {t('auth.confirmEmail.resend')}
                        </Typography>
                        <TextField
                            variant="outlined"
                            fullWidth
                            label={t('auth.confirmEmail.resendEmailLabel')}
                            value={resendEmail}
                            disabled={isLoading}
                            onChange={(e) => setResendEmail(e.target.value)}
                        />
                        <Box mt={2} position="relative">
                            <Button
                                variant="contained"
                                color="primary"
                                disabled={!resendEmail || isLoading}
                                onClick={() => dispatch(resendConfirmation(resendEmail))}
                            >
                                {t('auth.confirmEmail.resendSubmit')}
                            </Button>
                            {isLoading && <CircularProgress size={24}/>}
                        </Box>
                    </>
                )}
            </Box>
        </Container>
    );
};

export default ConfirmEmail;
