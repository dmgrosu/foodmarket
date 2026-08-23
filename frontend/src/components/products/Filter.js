import React from 'react';
import {FormControl, FormGroup, IconButton, InputLabel, MenuItem, Select, TextField} from "@material-ui/core";
import {withStyles} from "@material-ui/styles";
import SearchIcon from "@material-ui/icons/Search";
import {useTranslation} from "react-i18next";

const styles = theme => ({
    formControl: {
        margin: theme.spacing(2),
        minWidth: 200,
    },
    formButton: {
        position: 'relative',
        top: '20%'
    }
});

const Filter = ({storages, storageId, brands, brandId, name, changeFilter, classes, search, changed}) => {

    const {t} = useTranslation();

    const brandsItems = brands.length > 0 && brands
        .map(brand => <MenuItem key={brand.id} value={brand.id}>{brand.name}</MenuItem>);
    const storageItems = storages.length > 0 && storages
        .map(storage => <MenuItem key={storage.id} value={storage.id}>{storage.name}</MenuItem>);

    return (
        <FormGroup row>
            <FormControl className={classes.formControl}>
                <InputLabel id="storage-id-label">{t('products.filter.storage')}</InputLabel>
                <Select labelId="storage-id-label"
                        value={storageId}
                        onChange={(e) => changeFilter(e, 'storageId')}
                >
                    <MenuItem key={0} value={0}><em>{t('common.all')}</em></MenuItem>
                    {storageItems}
                </Select>
            </FormControl>
            <FormControl className={classes.formControl}>
                <InputLabel id="brand-id-label">{t('products.filter.brand')}</InputLabel>
                <Select labelId="brand-id-label"
                        value={brandId}
                        onChange={(e) => changeFilter(e, 'brandId')}
                >
                    <MenuItem key={0} value={0}><em>{t('common.all')}</em></MenuItem>
                    {brandsItems}
                </Select>
            </FormControl>
            <FormControl className={classes.formControl}>
                <TextField value={name}
                           label={t('products.filter.name')}
                           onChange={(e) => changeFilter(e, 'name')}
                />
            </FormControl>
            <FormControl>
                <IconButton onClick={search}
                            className={classes.formButton}
                            disabled={!changed}
                >
                    <SearchIcon fontSize="large"/>
                </IconButton>
            </FormControl>
        </FormGroup>
    )
}

export default withStyles(styles)(Filter);
