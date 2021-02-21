import React from 'react';
import {AppBar, Button, IconButton, Toolbar, Typography} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";
import {MenuIcon} from "@material-ui/data-grid";
import ShoppingCartIcon from '@material-ui/icons/ShoppingCart';

const useStyles = makeStyles((theme) => ({
    root: {
        flexGrow: 1,
    },
    menuButton: {
        marginRight: theme.spacing(2),
    },
    title: {
        flexGrow: 1,
    },
}));

const Navbar = ({authorized}) => {

    const classes = useStyles();

    return (
        <AppBar position="static">
            <Toolbar>
                <IconButton edge="start" className={classes.menuButton} color="inherit" aria-label="menu">
                    <MenuIcon/>
                </IconButton>
                <Typography variant="h6" className={classes.title}>
                    Ramaiana food market
                </Typography>
                {!authorized && <Button color="inherit" href="/signIn">
                    Login
                </Button>}
                {authorized && <div>
                    <IconButton>
                        <ShoppingCartIcon/>
                    </IconButton>
                    <Button color="inherit" href="/signIn">
                        Logout
                    </Button>
                </div>}
            </Toolbar>
        </AppBar>
    )
}

export default Navbar;
