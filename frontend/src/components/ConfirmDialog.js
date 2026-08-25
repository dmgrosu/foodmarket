import React from 'react';
import {Button, Dialog, DialogActions, DialogTitle} from "@material-ui/core";
import {useTranslation} from "react-i18next";

const ConfirmDialog = ({isOpen, title, onCancel, onOk}) => {
    const {t} = useTranslation();
    return (
        <Dialog
            open={isOpen}
            onClose={onCancel}
            aria-labelledby="dialog-title"
        >
            <DialogTitle id="dialog-title">{title}</DialogTitle>
            <DialogActions>
                <Button onClick={onOk}
                        color="primary"
                >
                    {t('common.ok')}
                </Button>
                <Button onClick={onCancel}
                        autoFocus
                >
                    {t('common.cancel')}
                </Button>
            </DialogActions>
        </Dialog>
    )
}

export default ConfirmDialog;    
