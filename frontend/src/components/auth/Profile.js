import React, {useCallback, useEffect, useState} from 'react';
import {Button, CircularProgress, Container, Divider, Grid, MenuItem, Paper, Select, TextField, Typography} from "@material-ui/core";
import {useTranslation} from "react-i18next";
import {toast} from "material-react-toastify";
import {changePassword, getProfile, updateProfile} from "../../api/profile";
import {handleError} from "../../store/actions/authActions";
import {VALIDATION_MESSAGE_KEYS} from "../../i18n/validationCodes";

// Same list and labels the navbar's LanguageSwitcher offers, so the two never drift apart.
const LANGUAGES = ["ru", "ro", "en"];

// Shared with SignUp so every screen that sets a password agrees on what a valid one is.
const PASSWORD_REGEXP = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,}$/;

const sectionStyle = {padding: 24, marginTop: 24};

/**
 * The signed-in user's own account.
 *
 * Name and language are editable and saved together in one call; email, state, roles and the client
 * company are read-only. The client block in particular is imported from the ERP feed and would be
 * overwritten on the next import, so offering to edit it here would be a lie.
 */
const Profile = () => {
    const {t, i18n} = useTranslation();

    const [profile, setProfile] = useState(null);
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [language, setLanguage] = useState(i18n.resolvedLanguage || 'ru');
    const [isLoading, setIsLoading] = useState(true);
    const [isSavingProfile, setIsSavingProfile] = useState(false);

    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [isSavingPassword, setIsSavingPassword] = useState(false);
    // Stored as {field, code} and translated at render time, so switching language after a failed
    // submit re-translates the message instead of leaving it frozen — same as SignIn/SignUp.
    const [errors, setErrors] = useState([]);

    const applyProfile = useCallback((data) => {
        setProfile(data);
        setFirstName(data.firstName || '');
        setLastName(data.lastName || '');
        setLanguage(data.language);
    }, []);

    useEffect(() => {
        getProfile()
            .then(applyProfile)
            .catch(handleError)
            .finally(() => setIsLoading(false));
    }, [applyProfile]);

    const getErrorForField = (fieldName) => {
        const error = errors.find(err => err.field === fieldName);
        return error ? t(VALIDATION_MESSAGE_KEYS[error.code]) : false;
    };

    const changePasswordField = (setter, field) => (e) => {
        setter(e.target.value);
        setErrors(current => current.filter(err => err.field !== field));
    };

    const saveProfile = () => {
        setIsSavingProfile(true);
        updateProfile({firstName, lastName, language})
            .then(data => {
                applyProfile(data);
                // Follow the language that was just persisted, so the UI cannot disagree with it.
                i18n.changeLanguage(data.language);
                toast.success(t('profile.saved'));
            })
            .catch(handleError)
            .finally(() => setIsSavingProfile(false));
    };

    const validatePassword = () => {
        const found = [];
        if (!currentPassword) {
            found.push({field: 'currentPassword', code: 'CURRENT_PASSWORD_EMPTY'});
        }
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

    const savePassword = () => {
        if (!validatePassword()) {
            return;
        }
        setIsSavingPassword(true);
        changePassword({currentPassword, newPassword})
            .then(() => {
                setCurrentPassword('');
                setNewPassword('');
                setConfirmPassword('');
                toast.success(t('profile.password.saved'));
            })
            .catch(handleError)
            .finally(() => setIsSavingPassword(false));
    };

    if (isLoading) {
        return (
            <Container maxWidth="sm">
                <div style={{marginTop: 32, textAlign: 'center'}}>
                    <CircularProgress/>
                </div>
            </Container>
        );
    }

    if (!profile) {
        return (
            <Container maxWidth="sm">
                <Typography color="error" style={{marginTop: 32}}>{t('profile.loadFailed')}</Typography>
            </Container>
        );
    }

    return (
        <Container maxWidth="sm">
            <Typography component="h1" variant="h5" style={{marginTop: 24}}>
                {t('profile.title')}
            </Typography>

            <Paper style={sectionStyle}>
                <Typography variant="h6" gutterBottom>{t('profile.account.title')}</Typography>
                <Grid container spacing={2}>
                    <Grid item xs={12} sm={6}>
                        <TextField
                            variant="outlined"
                            fullWidth
                            id="firstName"
                            label={t('auth.fields.firstName')}
                            value={firstName}
                            disabled={isSavingProfile}
                            onChange={(e) => setFirstName(e.target.value)}
                        />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                        <TextField
                            variant="outlined"
                            fullWidth
                            id="lastName"
                            label={t('auth.fields.lastName')}
                            value={lastName}
                            disabled={isSavingProfile}
                            onChange={(e) => setLastName(e.target.value)}
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <Typography variant="body2" color="textSecondary">
                            {t('auth.fields.email')}
                        </Typography>
                        <Typography>{profile.email}</Typography>
                    </Grid>
                    <Grid item xs={12}>
                        <Typography variant="body2" color="textSecondary">
                            {t('profile.account.state')}
                        </Typography>
                        <Typography>{profile.state}</Typography>
                    </Grid>
                    {profile.client && <Grid item xs={12}>
                        <Divider style={{marginBottom: 16}}/>
                        <Typography variant="body2" color="textSecondary">
                            {t('profile.account.company')}
                        </Typography>
                        <Typography>{profile.client.name}</Typography>
                        <Typography variant="body2" color="textSecondary">
                            {t('auth.fields.idno')}: {profile.client.idno}
                        </Typography>
                    </Grid>}
                    <Grid item xs={12}>
                        <Typography variant="body2" color="textSecondary" gutterBottom>
                            {t('profile.language.label')}
                        </Typography>
                        <Select value={language}
                                onChange={(e) => setLanguage(e.target.value)}
                                disabled={isSavingProfile}
                                variant="outlined"
                                fullWidth
                        >
                            {LANGUAGES.map(lng => (
                                <MenuItem key={lng} value={lng}>{t(`language.${lng}`)}</MenuItem>
                            ))}
                        </Select>
                    </Grid>
                    <Grid item xs={12}>
                        <Button variant="contained"
                                color="primary"
                                disabled={isSavingProfile}
                                onClick={saveProfile}
                        >
                            {t('profile.save')}
                        </Button>
                        {isSavingProfile && <CircularProgress size={24} style={{marginLeft: 12}}/>}
                    </Grid>
                </Grid>
            </Paper>

            <Paper style={sectionStyle}>
                <Typography variant="h6" gutterBottom>{t('profile.password.title')}</Typography>
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <TextField
                            variant="outlined"
                            fullWidth
                            required
                            type="password"
                            id="currentPassword"
                            label={t('profile.password.current')}
                            value={currentPassword}
                            autoComplete="current-password"
                            disabled={isSavingPassword}
                            error={getErrorForField('currentPassword') !== false}
                            helperText={getErrorForField('currentPassword')}
                            onChange={changePasswordField(setCurrentPassword, 'currentPassword')}
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <TextField
                            variant="outlined"
                            fullWidth
                            required
                            type="password"
                            id="newPassword"
                            label={t('profile.password.new')}
                            value={newPassword}
                            autoComplete="new-password"
                            disabled={isSavingPassword}
                            error={getErrorForField('newPassword') !== false}
                            helperText={getErrorForField('newPassword')}
                            onChange={changePasswordField(setNewPassword, 'newPassword')}
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <TextField
                            variant="outlined"
                            fullWidth
                            required
                            type="password"
                            id="confirmPassword"
                            label={t('auth.fields.confirmPassword')}
                            value={confirmPassword}
                            autoComplete="new-password"
                            disabled={isSavingPassword}
                            error={getErrorForField('confirmPassword') !== false}
                            helperText={getErrorForField('confirmPassword')}
                            onChange={changePasswordField(setConfirmPassword, 'confirmPassword')}
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <Button variant="contained"
                                color="primary"
                                disabled={isSavingPassword}
                                onClick={savePassword}
                        >
                            {t('profile.password.submit')}
                        </Button>
                        {isSavingPassword && <CircularProgress size={24} style={{marginLeft: 12}}/>}
                    </Grid>
                </Grid>
            </Paper>
        </Container>
    );
};

export default Profile;
