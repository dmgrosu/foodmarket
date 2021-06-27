import React from 'react';
import {CircularProgress, IconButton, Table, TableBody, TableCell, TableContainer, TableFooter, TableHead, TablePagination, TableRow, withStyles} from "@material-ui/core";
import moment from "moment";
import ArrowDownwardIcon from '@material-ui/icons/ArrowDownward';
import ArrowUpwardIcon from '@material-ui/icons/ArrowUpward';

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
    },
    row: {
        cursor: 'pointer'
    }
});

const OrdersList = ({
                        isFetching, classes, orders, pagination, changeSorting, viewOrder,
                        changeCurrentPage, changePageSize, sortColumn, sortDirection
                    }) => {

    const columns = [
        {id: 1, label: 'Date', align: 'left', minWidth: '20%', dataId: 'createdAt', dataType: 'date', sortable: true},
        {id: 2, label: 'Sum', align: 'right', minWidth: '20%', dataId: 'totalSum', dataType: 'number', sortable: true},
        {id: 3, label: 'Weight', align: 'right', minWidth: '20%', dataId: 'totalWeight', dataType: 'number', sortable: false},
        {id: 4, label: 'State', align: 'center', minWidth: '40%', dataId: 'state', dataType: 'string', sortable: true},
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
                                <IconButton size="small"
                                            onClick={() => column.sortable ? changeSorting(column.dataId) : null}
                                >
                                    {column.label}
                                    {column.dataId === sortColumn && sortDirection && <ArrowDownwardIcon fontSize="small"/>}
                                    {column.dataId === sortColumn && !sortDirection && <ArrowUpwardIcon fontSize="small"/>}
                                </IconButton>
                            </TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {Array.isArray(orders) ? orders.map(order => (
                        <TableRow key={order.id}
                                  hover
                                  onClick={() => viewOrder(order)}
                                  className={classes.row}
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
