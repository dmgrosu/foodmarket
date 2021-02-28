import React from 'react';
import {Typography} from "@material-ui/core";
import {withStyles} from "@material-ui/styles";

const styles = (theme) => ({
    paper: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
    },
});

const Home = ({classes}) => {
    return (
        <Typography paragraph className={classes.paper}>
            Here will be implemented home page
        </Typography>
    )
}

export default withStyles(styles)(Home);
