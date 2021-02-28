import React, {Component} from 'react';
import {Button, Dialog, DialogActions, DialogTitle, IconButton, Menu, MenuItem} from "@material-ui/core";
import {Link} from "react-router-dom";
import {AccountCircle} from "@material-ui/icons";

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
        const {isAuthenticated} = this.props;

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
                    {isAuthenticated && <MenuItem component={Link} to="/profile">Profile</MenuItem>}
                    {isAuthenticated && <MenuItem onClick={this.toggleDialog}>Logout</MenuItem>}
                    {!isAuthenticated && <MenuItem component={Link} to="/signIn">Login</MenuItem>}
                    {!isAuthenticated && <MenuItem component={Link} to="/signUp">Sign Up</MenuItem>}
                </Menu>
                <Dialog
                    open={dialogOpen}
                    onClose={this.toggleDialog}
                    aria-labelledby="alert-dialog-title"
                >
                    <DialogTitle id="alert-dialog-title">{"Do you want to logout?"}</DialogTitle>
                    <DialogActions>
                        <Button onClick={this.handleLogout} color="primary">
                            OK
                        </Button>
                        <Button onClick={this.toggleDialog} color="primary" autoFocus>
                            Cancel
                        </Button>
                    </DialogActions>
                </Dialog>

            </div>
        )
    }
}

export default RightMenu;
