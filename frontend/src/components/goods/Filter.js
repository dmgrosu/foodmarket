import React from 'react';
import {FormControl, FormGroup, IconButton, InputLabel, MenuItem, Select, TextField} from "@material-ui/core";
import {withStyles} from "@material-ui/styles";
import SearchIcon from "@material-ui/icons/Search";

const styles = theme => ({
    formControl: {
        margin: theme.spacing(2),
        minWidth: 200,
    },
});

const Filter = ({brands, brandId, name, changeFilter, classes, search}) => {

    const menuItems = brands.length > 0 && brands
        .map(brand => <MenuItem key={brand.id} value={brand.id}>{brand.name}</MenuItem>);

    return (
        <FormGroup row>
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
            <FormControl className={classes.formControl}>
                <TextField value={name}
                           label="name"
                           onChange={(e) => changeFilter(e, 'name')}
                />
            </FormControl>
            <FormControl>
                <IconButton onClick={search}>
                    <SearchIcon/>
                </IconButton>
            </FormControl>
        </FormGroup>
    )
}

export default withStyles(styles)(Filter);