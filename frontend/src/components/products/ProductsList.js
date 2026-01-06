import React from 'react';
import {CircularProgress, IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, withStyles} from "@material-ui/core";
import {AddShoppingCart} from "@material-ui/icons";

const styles = theme => ({
    container: {
        height: 600,
    },
    head: {
        backgroundColor: '#bdbdbd',
    },
    progress: {
        margin: 'auto',
        display: 'flex',
        position: 'relative',
        top: '40%',
    }
});

const ProductsList = ({classes, products, handleSelect, isFetching}) => {

    const columns = [
        {id: 1, label: 'Name', align: 'left', minWidth: '40%', dataId: 'name'},
        {id: 2, label: 'Price', align: 'center', minWidth: '30%', dataId: 'price'},
        {id: 3, label: 'Bar-code', align: 'left', minWidth: '10%', dataId: 'barCode'},
        {id: 4, label: 'Package', align: 'right', minWidth: '10%', dataId: 'package'},
        {id: 5, label: 'Unit', align: 'left', minWidth: '10%', dataId: 'unit'},
    ];

    if (isFetching) {
        return (
            <div className={classes.container}>
                <CircularProgress className={classes.progress} size={60}/>
            </div>
        )
    }

    return (
        <TableContainer className={classes.container}>
            <Table stickyHeader
                   size="small"
            >
                <TableHead>
                    <TableRow>
                        <TableCell style={{width: 20}} className={classes.head}/>
                        {columns.map(column => (
                            <TableCell key={column.id}
                                       align={column.align}
                                       style={{minWidth: column.minWidth}}
                                       className={classes.head}
                            >
                                {column.label}
                            </TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {Array.isArray(products) ? products.map(product => (
                        <TableRow key={product.id}
                                  hover
                        >
                            <TableCell>
                                <IconButton onClick={() => handleSelect(product.id)}>
                                    <AddShoppingCart fontSize="small" color="secondary"/>
                                </IconButton>
                            </TableCell>
                            {columns.map(column => {
                                const value = product[column.dataId];
                                return (
                                    <TableCell key={column.id}
                                               align={column.align}
                                    >
                                        {typeof value === 'number' ? value.toFixed(2) : value}
                                    </TableCell>
                                )
                            })}
                        </TableRow>
                    )) : null}
                </TableBody>
            </Table>
        </TableContainer>
    )
}

export default withStyles(styles)(ProductsList);
