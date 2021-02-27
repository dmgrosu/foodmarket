import React from 'react';
import {Link} from "react-router-dom";
import {ListItem, ListItemIcon, ListItemText} from "@material-ui/core";

const SidebarItem = ({text, linkTo, icon}) => {
    return (
        <ListItem button
                  key={text}
                  component={Link} to={linkTo}>
            <ListItemIcon>
                {icon}
            </ListItemIcon>
            <ListItemText primary={text}/>
        </ListItem>
    )
};

export default SidebarItem;
