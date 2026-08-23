import React from 'react';
import {Button, Grid, IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography, withStyles} from "@material-ui/core";
import {connect} from "react-redux";
import {Delete} from "@material-ui/icons";
import {cancelDeleteProduct, closePlaceOrderDialog, deleteProductFromCart, openPlaceOrderDialog, placeOrder, selectProductToDelete} from "../../store/actions/cartActions";
import {Link} from "react-router-dom";
import ConfirmDialog from "../ConfirmDialog";
import {useTranslation} from "react-i18next";

const styles = theme => ({
    root: {
        padding: theme.spacing(2),
        maxWidth: 1400,
        margin: "auto"
    },
    toolbar: {
        paddingBottom: theme.spacing(2),
    },
    totalBar: {
        textAlign: "right",
    },
    table: {
        maxHeight: 600,
        margin: 'auto',
        paddingBottom: theme.spacing(2),
    },
    head: {
        backgroundColor: '#bdbdbd',
    },
    total: {
        fontWeight: 'bold'
    }
});

const Cart = ({classes, cart, selectProductToDelete, cancelDeleteProduct, deleteProductFromCart,
                  openPlaceOrderDialog, closePlaceOrderDialog, placeOrder}) => {

    const {t} = useTranslation();

    const columns = [
        {id: 1, label: t('cart.columns.name'), align: 'left', minWidth: '40%', dataId: 'productName'},
        {id: 2, label: t('cart.columns.price'), align: 'center', minWidth: '20%', dataId: 'price'},
        {id: 3, label: t('cart.columns.quantity'), align: 'center', minWidth: '20%', dataId: 'quantity'},
        {id: 4, label: t('cart.columns.sum'), align: 'right', minWidth: '20%', dataId: 'sum'},
    ];

    const products = cart.products;

    return (
        <Grid container className={classes.root}>
            <Grid item container sm={10} className={classes.toolbar}>
                <Grid item sm={8}>
                    <Typography variant="h5">
                        {t('cart.title')}
                    </Typography>
                </Grid>
                <Grid item sm={4} style={{textAlign: "right"}}>
                    <Button variant="contained"
                            component={Link} to="/products">
                        {t('cart.backToCatalogue')}
                    </Button>
                </Grid>
            </Grid>
            <Grid item sm={10} className={classes.table}>
                <TableContainer>
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
                                <TableRow key={product.productId}
                                          hover
                                >
                                    <TableCell>
                                        <IconButton onClick={() => selectProductToDelete(product.productId)}>
                                            <Delete fontSize="small" color="secondary"/>
                                        </IconButton>
                                    </TableCell>
                                    {columns.map(column => {
                                        let value = product[column.dataId];
                                        if (column.dataId === 'price') {
                                            value = product.sum / product.quantity;
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
                                <TableCell colSpan={4} className={classes.total}>{t('cart.total')}</TableCell>
                                <TableCell align="right" className={classes.total}>
                                    {Array.isArray(products) ?
                                        products
                                            .reduce((accumulator, currentValue) => accumulator + currentValue.sum, 0)
                                            .toFixed(2) :
                                        0}
                                </TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </TableContainer>
            </Grid>
            <Grid item sm={2} className={classes.totalBar}>
                <Button variant="contained"
                        color="secondary"
                        onClick={openPlaceOrderDialog}
                >
                    {t('cart.placeOrder')}
                </Button>
            </Grid>
            <ConfirmDialog isOpen={cart.deleteProductId !== null}
                           onCancel={cancelDeleteProduct}
                           onOk={() => deleteProductFromCart(cart.orderId, cart.deleteProductId)}
                           title={t('cart.confirmRemove')}
            />
            <ConfirmDialog isOpen={cart.placeOrderDialogOpen}
                           onCancel={closePlaceOrderDialog}
                           onOk={() => placeOrder(cart.orderId)}
                           title={t('cart.confirmPlaceOrder')}
            />
        </Grid>
    )
}

const mapStateToProps = state => ({
    cart: state.cartReducer
});

export default connect(mapStateToProps, {
    selectProductToDelete,
    deleteProductFromCart: deleteProductFromCart,
    cancelDeleteProduct,
    closePlaceOrderDialog,
    openPlaceOrderDialog,
    placeOrder
})(withStyles(styles)(Cart));
