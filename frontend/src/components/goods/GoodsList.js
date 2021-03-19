import React from 'react';
import {IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, withStyles} from "@material-ui/core";
import {AddShoppingCart} from "@material-ui/icons";

const styles = theme => ({
    container: {
        height: 600,
    },
    tableHeader: {
        background: theme.palette.secondary
    }
});

const GoodsList = ({classes, goods, handleSelect}) => {

    const columns = [
        {id: 1, label: 'Name', align: 'left', minWidth: 400, dataId: 'name'},
        {id: 2, label: 'Price', align: 'center', minWidth: 100, dataId: 'price'},
        {id: 3, label: 'Bar-code', align: 'left', minWidth: 50, dataId: 'barCode'},
        {id: 4, label: 'Package', align: 'right', minWidth: 100, dataId: 'package'},
        {id: 5, label: 'Unit', align: 'left', minWidth: 100, dataId: 'unit'},
    ]

    return (
        <TableContainer className={classes.container}>
            <Table stickyHeader
                   className={classes.tableHeader}
                   size="small"
            >
                <TableHead>
                    <TableRow>
                        <TableCell style={{width: 30}}/>
                        {columns.map(column => (
                            <TableCell key={column.id}
                                       align={column.align}
                                       style={{minWidth: column.minWidth}}
                            >
                                {column.label}
                            </TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {Array.isArray(goods) ? goods.map(good => (
                        <TableRow key={good.id}
                                  hover
                        >
                            <TableCell>
                                <IconButton onClick={() => handleSelect(good.id)}>
                                    <AddShoppingCart fontSize="small"/>
                                </IconButton>
                            </TableCell>
                            {columns.map(column => {
                                const value = good[column.dataId];
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

export default withStyles(styles)(GoodsList);
