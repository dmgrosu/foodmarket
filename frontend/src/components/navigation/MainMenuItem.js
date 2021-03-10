import React from 'react';
import {Link} from "react-router-dom";
import {ListItemIcon, ListItemText, MenuItem} from "@material-ui/core";

const MainMenuItem = ({text, linkTo, icon}) => {
    return (
        <MenuItem component={Link} to={linkTo}>
            <ListItemIcon>
                {icon}
            </ListItemIcon>
            <ListItemText primary={text}/>
        </MenuItem>
    )
};

export default MainMenuItem;
