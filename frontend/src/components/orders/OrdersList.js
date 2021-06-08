import React from 'react';
import {CircularProgress, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, withStyles} from "@material-ui/core";

const styles = () => ({
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

const OrdersList = ({isFetching, classes, orders}) => {

    const columns = [
        {id: 1, label: 'Date', align: 'left', minWidth: '20%', dataId: 'date'},
        {id: 2, label: 'Sum', align: 'right', minWidth: '20%', dataId: 'totalSum'},
        {id: 3, label: 'Weight', align: 'right', minWidth: '20%', dataId: 'totalWeight'},
        {id: 4, label: 'State', align: 'center', minWidth: '40%', dataId: 'state'},
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
                    {Array.isArray(orders) ? orders.map(order => (
                        <TableRow key={order.id}
                                  hover
                        >
                            {columns.map(column => {
                                const value = order[column.dataId];
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

export default withStyles(styles)(OrdersList);
