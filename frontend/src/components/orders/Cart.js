import React from 'react';
import {Card, CardActions, CardContent, CardHeader, IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, withStyles} from "@material-ui/core";
import {connect} from "react-redux";
import {Delete} from "@material-ui/icons";

const styles = theme => ({
    container: {
        height: 600,
        maxWidth: 1000,
        margin: 'auto'
    },
    head: {
        backgroundColor: '#bdbdbd',
    }
});

const Cart = ({classes, cart}) => {

    const columns = [
        {id: 1, label: 'Name', align: 'left', minWidth: '40%', dataId: 'goodName'},
        {id: 2, label: 'Price', align: 'center', minWidth: '20%', dataId: 'price'},
        {id: 3, label: 'Quantity', align: 'center', minWidth: '20%', dataId: 'quantity'},
        {id: 4, label: 'Sum', align: 'right', minWidth: '20%', dataId: 'sum'},
    ];

    const goods = cart.goods;

    const totalCalculator = (accumulator, currentValue) => accumulator + currentValue.sum;

    return (
        <Card>
            <CardHeader>

            </CardHeader>
            <CardContent>
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
                            {Array.isArray(goods) ? goods.map(good => (
                                <TableRow key={good.goodId}
                                          hover
                                >
                                    <TableCell>
                                        <IconButton>
                                            <Delete fontSize="small" color="secondary"/>
                                        </IconButton>
                                    </TableCell>
                                    {columns.map(column => {
                                        let value = good[column.dataId];
                                        if (column.dataId === 'price') {
                                            value = good.sum / good.quantity;
                                        }
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
                            <TableRow>
                                <TableCell colSpan={4}>Total</TableCell>
                                <TableCell align="right">
                                    {Array.isArray(goods) ? goods.reduce(totalCalculator, 0).toFixed(2) : 0}
                                </TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </TableContainer>
            </CardContent>
            <CardActions>

            </CardActions>
        </Card>
    )
}

const mapStateToProps = state => ({
    cart: state.cartReducer
});

export default connect(mapStateToProps, {})(withStyles(styles)(Cart));
