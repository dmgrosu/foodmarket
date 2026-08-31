import React, {useState} from 'react';
import {Link, Redirect, useLocation} from 'react-router-dom';
import {useDispatch, useSelector} from 'react-redux';
import {useTranslation} from 'react-i18next';
import {toast} from 'material-react-toastify';
import {Box, Button, CircularProgress, Container, TextField, Typography} from '@material-ui/core';
import {resetPassword} from '../../store/actions/authActions';
import {VALIDATION_MESSAGE_KEYS} from '../../i18n/validationCodes';

// Shared with SignUp and Profile so every screen that sets a password agrees on what a valid one is.
const PASSWORD_REGEXP = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,}$/;

/**
 * Landing page for the link in a reset email: ?resetToken=... .
 *
 * A missing token and a token the backend rejects collapse into the same "invalid or expired" state.
 * That mirrors the backend, which answers unknown, already-used and expired tokens identically so it
 * never reveals whether a token existed — this page must not try to distinguish them either.
 */
const ResetPassword = () => {
    const {t} = useTranslation();
    const dispatch = useDispatch();
    const location = useLocation();
    const {isResetting, resetDone, resetError, token} = useSelector(state => state.authReducer);

    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    // {field, code}, translated at render time — same pattern as SignIn/SignUp/Profile.
    const [errors, setErrors] = useState([]);

    const resetToken = new URLSearchParams(location.search).get('resetToken');

    if (token) {
        return <Redirect to="/products"/>;
    }

    if (resetDone) {
        toast.success(t('auth.resetPassword.success'));
        return <Redirect to="/signIn"/>;
    }

    const getErrorForField = (fieldName) => {
        const error = errors.find(err => err.field === fieldName);
        return error ? t(VALIDATION_MESSAGE_KEYS[error.code]) : false;
    };

    const changeValue = (setter, field) => (e) => {
        setter(e.target.value);
        setErrors(current => current.filter(err => err.field !== field));
    };

    const validateInput = () => {
        const found = [];
        if (!newPassword) {
            found.push({field: 'newPassword', code: 'PASSWORD_EMPTY'});
        } else if (newPassword !== confirmPassword) {
            found.push({field: 'confirmPassword', code: 'PASSWORD_NOT_MATCH'});
        } else if (!PASSWORD_REGEXP.test(newPassword)) {
            found.push({field: 'newPassword', code: 'PASSWORD_NOT_STRONG'});
        }
        setErrors(found);
        return found.length === 0;
    };

    const submit = () => {
        if (validateInput()) {
            dispatch(resetPassword(resetToken, newPassword));
        }
    };

    const isInvalid = !resetToken || resetError;

    return (
        <Container component="main" maxWidth="xs">
            <Box mt={4} display="flex" flexDirection="column" alignItems="center" textAlign="center">
                <Typography component="h1" variant="h5" gutterBottom>
                    {t('auth.resetPassword.title')}
                </Typography>

                {isInvalid && (
                    <>
                        <Typography color="error" gutterBottom>
                            {t('auth.resetPassword.invalid')}
                        </Typography>
                        <Link to="/forgotPassword">{t('auth.resetPassword.requestNew')}</Link>
                    </>
                )}

                {!isInvalid && (
                    <>
                        <TextField
                            variant="outlined"
                            margin="normal"
                            fullWidth
                            required
                            type="password"
                            id="newPassword"
                            autoComplete="new-password"
                            autoFocus
                            label={t('auth.resetPassword.newPassword')}
                            value={newPassword}
                            disabled={isResetting}
                            error={getErrorForField('newPassword') !== false}
                            helperText={getErrorForField('newPassword')}
                            onChange={changeValue(setNewPassword, 'newPassword')}
                        />
                        <TextField
                            variant="outlined"
                            margin="normal"
                            fullWidth
                            required
                            type="password"
                            id="confirmPassword"
                            autoComplete="new-password"
                            label={t('auth.fields.confirmPassword')}
                            value={confirmPassword}
                            disabled={isResetting}
                            error={getErrorForField('confirmPassword') !== false}
                            helperText={getErrorForField('confirmPassword')}
                            onChange={changeValue(setConfirmPassword, 'confirmPassword')}
                        />
                        <Box mt={2} position="relative">
                            <Button
                                variant="contained"
                                color="primary"
                                disabled={isResetting}
                                onClick={submit}
                            >
                                {t('auth.resetPassword.submit')}
                            </Button>
                            {isResetting && <CircularProgress size={24}/>}
                        </Box>
                    </>
                )}
            </Box>
        </Container>
    );
};

export default ResetPassword;
