import React from 'react';
import {AppBar, Badge, IconButton, Toolbar, Typography} from "@material-ui/core";
import ShoppingCartIcon from '@material-ui/icons/ShoppingCart';
import {connect} from "react-redux";
import {logout} from "../../store/actions/authActions";
import {withStyles} from "@material-ui/styles";
import RightMenu from "./RightMenu";
import MainMenu from "./MainMenu";
import {Link} from "react-router-dom";

const styles = theme => ({
    title: {
        flexGrow: 1,
    },
    badge: {
        right: -3,
        top: 13,
        border: `2px solid ${theme.palette.background.paper}`,
        padding: '0 4px',
    },
});

const Navbar = (props) => {

    const {classes, auth, logout, cart} = props;
    const isAuthenticated = auth.token !== null;

    const getGoodsCount = () => {
        if (cart) {
            return cart.goods.length;
        }
    }

    return (
        <AppBar position="sticky">
            <Toolbar>
                <MainMenu isAuthenticated={isAuthenticated}/>
                <Typography variant="h6" className={classes.title}>
                    Ramaiana Food Market
                </Typography>
                {isAuthenticated && <IconButton component={Link} to="/cart">
                    <Badge badgeContent={getGoodsCount()} color="error">
                        <ShoppingCartIcon/>
                    </Badge>
                </IconButton>}
                <RightMenu isAuthenticated={isAuthenticated}
                           handleLogout={logout}
                />
            </Toolbar>
        </AppBar>
    )
}

const mapStateToProps = state => ({
    auth: state.authReducer,
    cart: state.cartReducer,
});

export default connect(mapStateToProps, {
    logout
})(withStyles(styles)(Navbar));
