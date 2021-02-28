import React from 'react';
import {AppBar, IconButton, Toolbar, Typography} from "@material-ui/core";
import ShoppingCartIcon from '@material-ui/icons/ShoppingCart';
import {connect} from "react-redux";
import {logout} from "../../store/actions/authActions";
import {withStyles} from "@material-ui/styles";
import RightMenu from "./RightMenu";
import MainMenu from "./MainMenu";

const styles = () => ({
    title: {
        flexGrow: 1,
    },
});

const Navbar = (props) => {

    const {classes, auth, logout} = props;
    const isAuthenticated = auth.token !== null;

    return (
        <AppBar position="sticky">
            <Toolbar>
                <MainMenu isAuthenticated={isAuthenticated}/>
                <Typography variant="h6" className={classes.title}>
                    Ramaiana Food Market
                </Typography>
                {isAuthenticated && <IconButton>
                    <ShoppingCartIcon/>
                </IconButton>}
                <RightMenu isAuthenticated={isAuthenticated}
                           handleLogout={logout}
                />
            </Toolbar>
        </AppBar>
    )
}

const mapStateToProps = state => ({
    auth: state.authReducer
});

export default connect(mapStateToProps, {
    logout
})(withStyles(styles)(Navbar));
