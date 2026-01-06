import React from 'react';
import {Button, Grid, IconButton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography, withStyles} from "@material-ui/core";
import {connect} from "react-redux";
import {Delete} from "@material-ui/icons";
import {cancelDeleteProduct, closePlaceOrderDialog, deleteProductFromCart, openPlaceOrderDialog, placeOrder, selectProductToDelete} from "../../store/actions/cartActions";
import {Link} from "react-router-dom";
import ConfirmDialog from "../ConfirmDialog";

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

    const columns = [
        {id: 1, label: 'Name', align: 'left', minWidth: '40%', dataId: 'productName'},
        {id: 2, label: 'Price', align: 'center', minWidth: '20%', dataId: 'price'},
        {id: 3, label: 'Quantity', align: 'center', minWidth: '20%', dataId: 'quantity'},
        {id: 4, label: 'Sum', align: 'right', minWidth: '20%', dataId: 'sum'},
    ];

    const products = cart.products;

    return (
        <Grid container className={classes.root}>
            <Grid item container sm={10} className={classes.toolbar}>
                <Grid item sm={8}>
                    <Typography variant="h5">
                        Shopping cart
                    </Typography>
                </Grid>
                <Grid item sm={4} style={{textAlign: "right"}}>
                    <Button variant="contained"
                            component={Link} to="/products">
                        Back to catalogue
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
                                <TableCell colSpan={4} className={classes.total}>Total</TableCell>
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
                    Place order
                </Button>
            </Grid>
            <ConfirmDialog isOpen={cart.deleteProductId !== null}
                           onCancel={cancelDeleteProduct}
                           onOk={() => deleteProductFromCart(cart.orderId, cart.deleteProductId)}
                           title="Remove selected product from cart?"
            />
            <ConfirmDialog isOpen={cart.placeOrderDialogOpen}
                           onCancel={closePlaceOrderDialog}
                           onOk={() => placeOrder(cart.orderId)}
                           title="You are about to place the new order. Please confirm!"
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
