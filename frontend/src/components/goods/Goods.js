import React, {Component} from 'react';
import {Grid, Typography} from "@material-ui/core";

class Goods extends Component {

    state = {

    }

    render() {
        return (
            <Grid container spacing={2}>
                <Grid item xs={12}>
                    <Typography>
                        {"Inside goods component"}
                    </Typography>
                </Grid>
            </Grid>
        )
    }
}

export default Goods;    
