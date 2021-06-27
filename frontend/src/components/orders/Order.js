import React from 'react';
import {Table, TableBody, TableCell, TableContainer, TableHead, TableRow, withStyles} from "@material-ui/core";
import moment from "moment";

const styles = () => ({
    container: {
        height: 600,
    },
    head: {
        backgroundColor: '#bdbdbd',
    },
});


const Order = ({order, classes}) => {

    const columns = [
        {id: 1, label: 'Good name', align: 'left', minWidth: '55%', dataId: 'goodName', dataType: 'string'},
        {id: 2, label: 'Quantity', align: 'center', minWidth: '15%', dataId: 'quantity', dataType: 'number'},
        {id: 3, label: 'Weight', align: 'center', minWidth: '15%', dataId: 'weight', dataType: 'number'},
        {id: 4, label: 'Sum', align: 'right', minWidth: '15%', dataId: 'sum', dataType: 'number'},
    ];

    const goods = order ? order.goods : [];

    return (
        <TableContainer className={classes.container}>
            <Table stickyHeader
                   size="small"
            >
                <TableHead>
                    <TableRow>
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
                            {columns.map(column => {
                                const value = good[column.dataId];
                                return (
                                    <TableCell key={column.id}
                                               align={column.align}
                                               style={{minWidth: column.minWidth}}
                                    >
                                        {
                                            column.dataType === 'number' ?
                                                value.toFixed(2) :
                                                column.dataType === 'date' ?
                                                    moment(Number(value)).local().format("DD.MM.YYYY HH:mm") :
                                                    value
                                        }
                                    </TableCell>
                                )
                            })}
                        </TableRow>
                    )) : null}
                    <TableRow>
                        <TableCell colSpan={2} className={classes.total}>Total</TableCell>
                        <TableCell align="center" className={classes.total}>
                            {Array.isArray(goods) ?
                                goods
                                    .reduce((accumulator, currentValue) => accumulator + currentValue.weight, 0)
                                    .toFixed(2) :
                                0}
                        </TableCell>
                        <TableCell align="right" className={classes.total}>
                            {Array.isArray(goods) ?
                                goods
                                    .reduce((accumulator, currentValue) => accumulator + currentValue.sum, 0)
                                    .toFixed(2) :
                                0}
                        </TableCell>
                    </TableRow>

                </TableBody>
            </Table>
        </TableContainer>

    )
}

export default withStyles(styles)(Order);
