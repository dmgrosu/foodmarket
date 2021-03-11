import React from 'react';
import {FormControl, Grid, IconButton, InputLabel, MenuItem, Select, TextField} from "@material-ui/core";
import {withStyles} from "@material-ui/styles";
import SearchIcon from "@material-ui/icons/Search";

const styles = theme => ({
    formControl: {
        margin: theme.spacing(2),
        minWidth: 140,
    },
});

const Filter = ({brands, brandId, name, changeFilter, classes, search}) => {

    const menuItems = brands.length > 0 && brands
        .map(brand => <MenuItem key={brand.id} value={brand.id}>{brand.name}</MenuItem>);

    return (
        <Grid container spacing={2} alignItems="center">
            <Grid item>
                <FormControl className={classes.formControl}>
                    <InputLabel id="brand-id-label">Brand</InputLabel>
                    <Select labelId="brand-id-label"
                            value={brandId}
                            onChange={(e) => changeFilter(e, 'brandId')}
                    >
                        <MenuItem key={0} value={0}><em>None</em></MenuItem>
                        {menuItems}
                    </Select>
                </FormControl>
            </Grid>
            <Grid item>
                <FormControl className={classes.formControl}>
                    <TextField value={name}
                               label="name"
                               onChange={(e) => changeFilter(e, 'name')}
                    />
                </FormControl>
            </Grid>
            <Grid item>
                <IconButton onClick={search}>
                    <SearchIcon/>
                </IconButton>
            </Grid>
        </Grid>
    )
}

export default withStyles(styles)(Filter);
