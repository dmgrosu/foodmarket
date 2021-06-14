import React from 'react';
import {CircularProgress, Table, TableBody, TableCell, TableContainer, TableFooter, TableHead, TablePagination, TableRow, withStyles} from "@material-ui/core";
import moment from "moment";

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

const OrdersList = ({
                        isFetching, classes, orders, pagination,
                        changeCurrentPage, changePageSize
                    }) => {

    const columns = [
        {id: 1, label: 'Date', align: 'left', minWidth: '20%', dataId: 'date', dataType: 'date'},
        {id: 2, label: 'Sum', align: 'right', minWidth: '20%', dataId: 'totalSum', dataType: 'number'},
        {id: 3, label: 'Weight', align: 'right', minWidth: '20%', dataId: 'totalWeight', dataType: 'number'},
        {id: 4, label: 'State', align: 'center', minWidth: '40%', dataId: 'state', dataType: 'string'},
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
                </TableBody>
                <TableFooter>
                    <TableRow>
                        <TablePagination
                            colSpan={4}
                            count={pagination.totalCount}
                            page={pagination.currentPage}
                            onChangePage={changeCurrentPage}
                            rowsPerPage={pagination.pageSize}
                            onChangeRowsPerPage={changePageSize}
                        />
                    </TableRow>
                </TableFooter>
            </Table>
        </TableContainer>
    )
}

export default withStyles(styles)(OrdersList);
