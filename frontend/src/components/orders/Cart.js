import React, {useEffect} from 'react';
import {
    Button,
    CircularProgress,
    Grid,
    IconButton,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    TextField,
    Typography,
    withStyles
} from "@material-ui/core";
import {connect} from "react-redux";
import {Delete} from "@material-ui/icons";
import {
    cancelDeleteProduct,
    clearCart,
    closePlaceOrderDialog,
    deleteProductFromCart,
    fetchCart,
    openPlaceOrderDialog,
    placeOrder,
    selectProductToDelete,
    updateCartProduct
} from "../../store/actions/cartActions";
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
    },
    quantityInput: {
        width: 90,
    },
    actions: {
        textAlign: "right",
        '& > *': {
            marginLeft: theme.spacing(1),
        },
    },
    empty: {
        padding: theme.spacing(4),
        textAlign: "center",
    }
});

const Cart = ({
                  classes, cart, selectProductToDelete, cancelDeleteProduct, deleteProductFromCart,
                  openPlaceOrderDialog, closePlaceOrderDialog, placeOrder, fetchCart,
                  updateCartProduct, clearCart
              }) => {

    const {t} = useTranslation();

    // The cart is server state; this page can be opened directly, so it loads its own copy.
    useEffect(() => {
        fetchCart();
    }, [fetchCart]);

    const products = Array.isArray(cart.products) ? cart.products : [];
    const isBusy = cart.isFetching || cart.isDeleting || cart.isUpdating || cart.isPlacing;

    const columns = [
        {id: 1, label: t('cart.columns.name'), align: 'left', minWidth: '40%'},
        {id: 2, label: t('cart.columns.price'), align: 'center', minWidth: '20%'},
        {id: 3, label: t('cart.columns.quantity'), align: 'center', minWidth: '20%'},
        {id: 4, label: t('cart.columns.sum'), align: 'right', minWidth: '20%'},
    ];

    const changeQuantity = (productId, value) => {
        const quantity = Number(value);
        if (quantity >= 1) {
            updateCartProduct(productId, quantity);
        }
    };

    const total = products.reduce((accumulator, product) => accumulator + product.sum, 0);

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
                {cart.isFetching && products.length === 0 ?
                    <div className={classes.empty}><CircularProgress size={28}/></div> :
                    products.length === 0 ?
                        <Typography className={classes.empty} color="textSecondary">
                            {t('cart.empty')}
                        </Typography> :
                        <TableContainer>
                            <Table stickyHeader size="small">
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
                                    {products.map(product => (
                                        <TableRow key={product.productId} hover>
                                            <TableCell>
                                                <IconButton
                                                    disabled={isBusy}
                                                    onClick={() => selectProductToDelete(product.productId)}>
                                                    <Delete fontSize="small" color="secondary"/>
                                                </IconButton>
                                            </TableCell>
                                            <TableCell align="left">{product.productName}</TableCell>
                                            <TableCell align="center">{product.price.toFixed(2)}</TableCell>
                                            <TableCell align="center">
                                                <TextField type="number"
                                                           size="small"
                                                           className={classes.quantityInput}
                                                           inputProps={{min: 1}}
                                                           defaultValue={product.quantity}
                                                           disabled={isBusy}
                                                           onBlur={event =>
                                                               changeQuantity(product.productId, event.target.value)}
                                                />
                                            </TableCell>
                                            <TableCell align="right">{product.sum.toFixed(2)}</TableCell>
                                        </TableRow>
                                    ))}
                                    <TableRow>
                                        <TableCell colSpan={4} className={classes.total}>
                                            {t('cart.total')}
                                        </TableCell>
                                        <TableCell align="right" className={classes.total}>
                                            {total.toFixed(2)}
                                        </TableCell>
                                    </TableRow>
                                </TableBody>
                            </Table>
                        </TableContainer>}
            </Grid>
            <Grid item sm={2} className={classes.actions}>
                <Button variant="contained"
                        color="secondary"
                        disabled={isBusy || products.length === 0}
                        onClick={openPlaceOrderDialog}
                >
                    {t('cart.placeOrder')}
                </Button>
                <Button disabled={isBusy || products.length === 0}
                        onClick={clearCart}
                >
                    {t('cart.clear')}
                </Button>
            </Grid>
            <ConfirmDialog isOpen={cart.deleteProductId !== null}
                           onCancel={cancelDeleteProduct}
                           onOk={() => deleteProductFromCart(cart.deleteProductId)}
                           title={t('cart.confirmRemove')}
            />
            <ConfirmDialog isOpen={cart.placeOrderDialogOpen}
                           onCancel={closePlaceOrderDialog}
                           onOk={() => placeOrder()}
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
    deleteProductFromCart,
    cancelDeleteProduct,
    closePlaceOrderDialog,
    openPlaceOrderDialog,
    placeOrder,
    fetchCart,
    updateCartProduct,
    clearCart
})(withStyles(styles)(Cart));
