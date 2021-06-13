import React from 'react';
import {Button, Dialog, DialogActions, DialogTitle} from "@material-ui/core";

const ConfirmDialog = ({isOpen, title, onCancel, onOk}) => {
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
                    OK
                </Button>
                <Button onClick={onCancel}
                        autoFocus
                >
                    Cancel
                </Button>
            </DialogActions>
        </Dialog>
    )
}

export default ConfirmDialog;    
