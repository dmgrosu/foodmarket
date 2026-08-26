import React, {useState} from 'react';
import {
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Typography
} from "@material-ui/core";
import {useTranslation} from "react-i18next";
import {activateUser} from "../../api/admin";

/**
 * Confirmation dialog for activating a single user. Deliberately a Material-UI Dialog rather than
 * window.confirm: it can show a per-attempt error inline and disable Confirm while the request is
 * in flight, neither of which a native confirm() supports.
 */
const ActivateUserDialog = ({user, onClose, onActivated}) => {
    const {t} = useTranslation();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const open = user !== null;

    const handleConfirm = () => {
        setIsSubmitting(true);
        setError(null);
        activateUser(user.id)
            .then(updatedUser => {
                setIsSubmitting(false);
                onActivated(updatedUser);
            })
            .catch(err => {
                setIsSubmitting(false);
                const message = err.response && err.response.data && err.response.data.message;
                setError(message || t('common.unknownError'));
            });
    };

    const handleClose = () => {
        if (isSubmitting) {
            return;
        }
        setError(null);
        onClose();
    };

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="xs" fullWidth>
            <DialogTitle>{t('admin.users.activate.title')}</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    {user && t('admin.users.activate.confirmText', {email: user.email})}
                </DialogContentText>
                {error && (
                    <Typography color="error" variant="body2">
                        {error}
                    </Typography>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose} disabled={isSubmitting}>
                    {t('common.cancel')}
                </Button>
                <Button
                    onClick={handleConfirm}
                    color="primary"
                    variant="contained"
                    disabled={isSubmitting}
                    startIcon={isSubmitting ? <CircularProgress size={16} color="inherit"/> : null}
                >
                    {t('admin.users.activate.confirm')}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default ActivateUserDialog;
