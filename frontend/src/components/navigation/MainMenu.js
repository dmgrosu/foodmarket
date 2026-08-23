import React, {Component} from 'react';
import {Divider, IconButton, ListItemIcon, ListItemText, Menu, MenuItem} from "@material-ui/core";
import HomeIcon from "@material-ui/icons/Home";
import MainMenuItem from "./MainMenuItem";
import {Assignment, ShopTwo, SupervisorAccount} from "@material-ui/icons";
import MenuIcon from "@material-ui/icons/Menu";
import {withStyles} from "@material-ui/styles";
import {Link} from "react-router-dom";

const styles = (theme) => ({
    menuButton: {
        marginRight: theme.spacing(2),
    },
});


class MainMenu extends Component {

    state = {
        open: null
    }

    openMenu = (event) => {
        this.setState({
            open: event.currentTarget
        })
    }

    render() {

        const open = this.state.open;
        const {isAuthenticated, isAdmin, classes} = this.props;

        return (
            <div>
                <IconButton edge="start"
                            color="inherit"
                            className={classes.menuButton}
                            onClick={e => this.openMenu(e)}
                >
                    <MenuIcon/>
                </IconButton>
                <Menu anchorEl={open}
                      keepMounted
                      open={open !== null}
                      onClick={() => this.setState({open: null})}
                      onClose={() => this.setState({open: null})}
                      anchorOrigin={{
                          vertical: "bottom",
                          horizontal: "left"
                      }}
                      getContentAnchorEl={null}
                >
                    <MenuItem component={Link} to="/">
                        <ListItemIcon>
                            <HomeIcon/>
                        </ListItemIcon>
                        <ListItemText primary="Домой"/>
                    </MenuItem>
                    {isAuthenticated && <Divider/>}
                    {isAuthenticated && <MainMenuItem text="Каталог" linkTo="/products" icon={<ShopTwo/>}/>}
                    {isAuthenticated && <MainMenuItem text="Заказы" linkTo="/orders" icon={<Assignment/>}/>}
                    {isAuthenticated && isAdmin && <Divider/>}
                    {isAuthenticated && isAdmin &&
                    <MainMenuItem text="Клиенты" linkTo="/admin" icon={<SupervisorAccount/>}/>}
                </Menu>
            </div>
        )
    }
}

export default withStyles(styles)(MainMenu);
