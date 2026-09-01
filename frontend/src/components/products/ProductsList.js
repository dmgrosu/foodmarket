import React from 'react';
import {
    CircularProgress,
    IconButton,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    withStyles
} from "@material-ui/core";
import {AddShoppingCart} from "@material-ui/icons";
import {useTranslation} from "react-i18next";

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

const ProductsList = ({
                          classes, products, handleSelect, isFetching,
                          pageNo, pageSize, totalProducts, rowsPerPageOptions,
                          changePage, changePageSize
                      }) => {

    const {t} = useTranslation();

    // groupName says which part of the catalogue a row came from, which matters once a search can
    // return products from anywhere in it rather than from the one open group.
    const columns = [
        {id: 1, label: t('products.columns.name'), align: 'left', minWidth: '35%', dataId: 'name'},
        {id: 2, label: t('products.columns.group'), align: 'left', minWidth: '20%', dataId: 'groupName'},
        {id: 3, label: t('products.columns.price'), align: 'center', minWidth: '15%', dataId: 'price'},
        {id: 4, label: t('products.columns.barCode'), align: 'left', minWidth: '10%', dataId: 'barCode'},
        {id: 5, label: t('products.columns.package'), align: 'right', minWidth: '10%', dataId: 'inPackage'},
        {id: 6, label: t('products.columns.unit'), align: 'left', minWidth: '10%', dataId: 'unit'},
    ];

    const pagination = (
        <TablePagination component="div"
                         rowsPerPageOptions={rowsPerPageOptions}
                         count={totalProducts}
                         page={pageNo}
                         rowsPerPage={pageSize}
                         onChangePage={(event, newPage) => changePage(newPage)}
                         onChangeRowsPerPage={event => changePageSize(parseInt(event.target.value, 10))}
                         labelRowsPerPage={t('products.table.rowsPerPage')}
                         labelDisplayedRows={({from, to, count}) => t('products.table.displayedRows', {
                             from,
                             to,
                             count
                         })}
        />
    );

    // The pager stays mounted while a page loads. Swapping it out for the spinner would make the
    // control the user just clicked disappear under the cursor on every page change.
    if (isFetching) {
        return (
            <>
                <div className={classes.container}>
                    <CircularProgress className={classes.progress} size={60}/>
                </div>
                {pagination}
            </>
        )
    }

    return (
        <>
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
        {pagination}
        </>
    )
}

export default withStyles(styles)(ProductsList);
