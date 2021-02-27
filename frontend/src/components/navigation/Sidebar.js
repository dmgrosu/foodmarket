import React from 'react';
import {Drawer, List, Toolbar} from "@material-ui/core";
import {Home, ShopTwo} from "@material-ui/icons";
import {withStyles} from "@material-ui/styles";
import SidebarItem from "./SidebarItem";

const drawerWidth = 180;

const styles = () => ({
    drawer: {
        width: drawerWidth,
        flexShrink: 0,
    },
    drawerPaper: {
        width: drawerWidth,
    },
    drawerContainer: {
        overflow: 'auto',
    },
});

const Sidebar = ({isAuthenticated, classes}) => {
    return (
        <Drawer className={classes.drawer}
                variant="permanent"
                classes={{
                    paper: classes.drawerPaper,
                }}
        >
            <Toolbar/>
            <List className={classes.drawerContainer}>
                <SidebarItem text="Home" linkTo="/" icon={<Home/>}/>
                {isAuthenticated && <SidebarItem text="Catalog" linkTo="/goods" icon={<ShopTwo/>}/>}
            </List>
        </Drawer>
    )
}

export default withStyles(styles)(Sidebar);
