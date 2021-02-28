import React from 'react';
import {Card, CardContent, Grid, Typography} from "@material-ui/core";
import {withStyles} from "@material-ui/styles";

const styles = (theme) => ({
    root: {
        flexGrow: 1,
        padding: theme.spacing(2)
    },
});

const Home = ({classes}) => {
    return (
        <Grid container spacing={2} className={classes.root}>
            <Grid item xs={12}>
                <Card>
                    <CardContent>
                        <Typography variant="h5">
                            Here will be implemented home page
                        </Typography>
                    </CardContent>
                </Card>
            </Grid>
            <Grid item xs={6}>
                <Card>
                    <CardContent>
                        <Typography variant="h6">
                            Part 1
                        </Typography>
                    </CardContent>
                </Card>
            </Grid>
            <Grid item xs={6}>
                <Card>
                    <CardContent>
                        <Typography variant="h6">
                            Part 2
                        </Typography>
                    </CardContent>
                </Card>
            </Grid>
        </Grid>
    )
}

export default withStyles(styles)(Home);
