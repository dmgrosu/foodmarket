import React, {Component} from 'react';
import {Button, Dialog, DialogActions, DialogTitle, IconButton, Menu, MenuItem} from "@material-ui/core";
import {Link} from "react-router-dom";
import {AccountCircle} from "@material-ui/icons";
import {withTranslation} from "react-i18next";

class RightMenu extends Component {

    state = {
        open: null,
        dialogOpen: false
    }

    openMenu = (event) => {
        this.setState({
            open: event.currentTarget
        })
    }

    toggleDialog = () => {
        this.setState(state => ({
            dialogOpen: !state.dialogOpen,
        }));
    }

    handleLogout = () => {
        this.toggleDialog();
        this.props.handleLogout();
    }

    render() {

        const {open, dialogOpen} = this.state;
        const {isAuthenticated, t} = this.props;

        return (
            <div>
                <IconButton color="inherit"
                            onClick={e => this.openMenu(e)}
                >
                    <AccountCircle/>
                </IconButton>
                <Menu anchorEl={open}
                      keepMounted
                      open={open !== null}
                      onClick={() => this.setState({open: null})}
                      onClose={() => this.setState({open: null})}
                      anchorOrigin={{
                          vertical: "bottom",
                          horizontal: "right"
                      }}
                      getContentAnchorEl={null}
                >
                    {isAuthenticated && <MenuItem component={Link} to="/profile">{t('nav.profile')}</MenuItem>}
                    {isAuthenticated && <MenuItem onClick={this.toggleDialog}>{t('nav.logout')}</MenuItem>}
                    {!isAuthenticated && <MenuItem component={Link} to="/signIn">{t('nav.signIn')}</MenuItem>}
                    {!isAuthenticated && <MenuItem component={Link} to="/signUp">{t('nav.signUp')}</MenuItem>}
                </Menu>
                <Dialog
                    open={dialogOpen}
                    onClose={this.toggleDialog}
                    aria-labelledby="alert-dialog-title"
                >
                    <DialogTitle id="alert-dialog-title">{t('nav.logoutConfirm')}</DialogTitle>
                    <DialogActions>
                        <Button onClick={this.handleLogout} color="primary">
                            {t('common.ok')}
                        </Button>
                        <Button onClick={this.toggleDialog} color="primary" autoFocus>
                            {t('common.cancel')}
                        </Button>
                    </DialogActions>
                </Dialog>

            </div>
        )
    }
}

export default withTranslation()(RightMenu);
