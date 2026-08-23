import React from 'react';
import {AppBar, Badge, IconButton, Toolbar, Typography} from "@material-ui/core";
import ShoppingCartIcon from '@material-ui/icons/ShoppingCart';
import {connect} from "react-redux";
import {logout} from "../../store/actions/authActions";
import {withStyles} from "@material-ui/styles";
import RightMenu from "./RightMenu";
import MainMenu from "./MainMenu";
import {Link} from "react-router-dom";
import {isAdmin} from "../../store/selectors/authSelectors";

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

    const {classes, auth, logout, cart, userIsAdmin} = props;
    const isAuthenticated = auth.token !== null;

    const getProductsCount = () => {
        if (cart) {
            return cart.products ? cart.products.length : 0;
        }
    }

    return (
        <AppBar position="sticky">
            <Toolbar>
                <MainMenu isAuthenticated={isAuthenticated} isAdmin={userIsAdmin}/>
                <img src="/logos/rama-dark.png" alt="Ramaiana" style={{height: 40, marginRight: 12}}/>
                <Typography variant="h6" className={classes.title}>
                    Ramaiana SRL
                </Typography>
                {isAuthenticated && <IconButton component={Link} to="/cart">
                    <Badge badgeContent={getProductsCount()} color="error">
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
    userIsAdmin: isAdmin(state),
});

export default connect(mapStateToProps, {
    logout
})(withStyles(styles)(Navbar));
