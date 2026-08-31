import React, {useState} from 'react';
import {Link, Redirect} from 'react-router-dom';
import {useDispatch, useSelector} from 'react-redux';
import {useTranslation} from 'react-i18next';
import {Box, Button, CircularProgress, Container, TextField, Typography} from '@material-ui/core';
import {forgotPassword} from '../../store/actions/authActions';

/**
 * Ask for a password reset link.
 *
 * The confirmation panel is deliberately neutral: the backend answers identically whether or not the
 * address is registered, so that no unauthenticated caller can use this page to discover who has an
 * account. The copy here must not appear to know more than the response does.
 */
const ForgotPassword = () => {
    const {t} = useTranslation();
    const dispatch = useDispatch();
    const {isLoading, resetRequestedFor, token} = useSelector(state => state.authReducer);
    const [email, setEmail] = useState('');

    if (token) {
        return <Redirect to="/products"/>;
    }

    return (
        <Container component="main" maxWidth="xs">
            <Box mt={4} display="flex" flexDirection="column" alignItems="center" textAlign="center">
                <Typography component="h1" variant="h5" gutterBottom>
                    {t('auth.forgotPassword.title')}
                </Typography>

                {resetRequestedFor ? (
                    <>
                        <Typography gutterBottom>{t('auth.forgotPassword.sent')}</Typography>
                        <Link to="/signIn">{t('auth.forgotPassword.backToSignIn')}</Link>
                    </>
                ) : (
                    <>
                        <Typography variant="subtitle2" gutterBottom>
                            {t('auth.forgotPassword.description')}
                        </Typography>
                        <TextField
                            variant="outlined"
                            margin="normal"
                            fullWidth
                            id="email"
                            name="email"
                            type="email"
                            autoComplete="email"
                            autoFocus
                            label={t('auth.fields.email')}
                            value={email}
                            disabled={isLoading}
                            onChange={(e) => setEmail(e.target.value)}
                        />
                        <Box mt={2} position="relative">
                            <Button
                                variant="contained"
                                color="primary"
                                disabled={!email || isLoading}
                                onClick={() => dispatch(forgotPassword(email))}
                            >
                                {t('auth.forgotPassword.submit')}
                            </Button>
                            {isLoading && <CircularProgress size={24}/>}
                        </Box>
                        <Box mt={2}>
                            <Link to="/signIn">{t('auth.forgotPassword.backToSignIn')}</Link>
                        </Box>
                    </>
                )}
            </Box>
        </Container>
    );
};

export default ForgotPassword;
