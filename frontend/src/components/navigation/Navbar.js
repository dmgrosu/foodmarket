import React, {Component} from 'react';
import {AppBar, IconButton, Menu, MenuItem, Toolbar, Typography} from "@material-ui/core";
import ShoppingCartIcon from '@material-ui/icons/ShoppingCart';
import {Link} from "react-router-dom";
import {AccountCircle} from "@material-ui/icons";
import {connect} from "react-redux";
import {logout} from "../../store/actions/authActions";
import {withStyles} from "@material-ui/styles";


const styles = (theme) => ({
    root: {
        flexGrow: 1,
    },
    menuButton: {
        marginRight: theme.spacing(2),
    },
    title: {
        flexGrow: 1,
    },
    appBar: {
        zIndex: theme.zIndex.drawer + 1,
    },
});

class Navbar extends Component {

    state = {
        menuOpen: null,
    }

    openMenu = (event) => {
        this.setState({
            menuOpen: event.currentTarget
        })
    }

    render = () => {

        const {classes, auth} = this.props;
        const isAuthenticated = auth.token !== null;
        const {menuOpen} = this.state;

        return (
            <AppBar position="fixed" className={classes.appBar}>
                <Toolbar>
                    <Typography variant="h6" className={classes.title}>
                        Ramaiana Food Market
                    </Typography>
                    {isAuthenticated && <IconButton>
                        <ShoppingCartIcon/>
                    </IconButton>}
                    <IconButton
                        aria-label="account of current user"
                        aria-controls="menu-appbar"
                        aria-haspopup="true"
                        onClick={e => this.openMenu(e)}
                        color="inherit"
                    >
                        <AccountCircle/>
                    </IconButton>
                    <Menu
                        id="menu-appbar"
                        anchorEl={menuOpen}
                        keepMounted
                        open={menuOpen !== null}
                        onClick={() => this.setState({menuOpen: null})}
                        onClose={() => this.setState({menuOpen: null})}
                    >
                        {isAuthenticated && <MenuItem component={Link} to="/profile">Profile</MenuItem>}
                        {isAuthenticated && <MenuItem onClick={this.props.logout}>Logout</MenuItem>}
                        {!isAuthenticated && <MenuItem component={Link} to="/signIn">Login</MenuItem>}
                        {!isAuthenticated && <MenuItem component={Link} to="/signUp">Sign Up</MenuItem>}
                    </Menu>
                </Toolbar>
            </AppBar>
        )
    }
}

const mapStateToProps = state => ({
    auth: state.authReducer
});

export default connect(mapStateToProps, {
    logout
})(withStyles(styles)(Navbar));
