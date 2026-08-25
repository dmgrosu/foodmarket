import axios from "../../axios-instance";
import {handleError} from "./authActions";
import i18n from "../../i18n";

export const ADD_TO_CART_START = "ADD_TO_CART_START";
export const ADD_TO_CART_SUCCESS = "ADD_TO_CART_SUCCESS";
export const ADD_TO_CART_FAIL = "ADD_TO_CART_FAIL";
export const SELECT_GOOD = "SELECT_GOOD";
export const CHANGE_QUANTITY = "CHANGE_QUANTITY";
export const DELETE_FROM_CART_START = "DELETE_FROM_CART_START";
export const DELETE_FROM_CART_CANCELLED = "DELETE_FROM_CART_CANCELLED";
export const DELETE_FROM_CART_END = "DELETE_FROM_CART_END";
export const SELECT_GOOD_TO_DELETE = "SELECT_GOOD_TO_DELETE";
export const PLACE_ORDER_START = "PLACE_ORDER_START";
export const PLACE_ORDER_SUCCESS = "PLACE_ORDER_SUCCESS";
export const PLACE_ORDER_FAIL = "PLACE_ORDER_FAIL";
export const OPEN_PLACE_ORDER_DIALOG = "OPEN_PLACE_ORDER_DIALOG";
export const CLOSE_PLACE_ORDER_DIALOG = "CLOSE_PLACE_ORDER_DIALOG";


export const addProductToCart = (productId, orderId, quantity) => {
    return (dispatch, getState) => {
        dispatch({type: ADD_TO_CART_START});
        axios.post("/order/addProduct",
            {orderId: orderId, productId: productId, quantity: quantity})
            .then(resp => {
                const data = resp.data;
                dispatch({
                    type: ADD_TO_CART_SUCCESS,
                    payload: {
                        orderId: data.id,
                        products: data.items,
                    }
                });
            })
            .catch(err => {
                dispatch({
                    type: ADD_TO_CART_FAIL,
                    payload: {
                        error: err.response ? err.response.status + ": " + err.response.data.message || err.response.statusText : i18n.t('common.unknownError')
                    }
                })
                handleError(err);
            })
    };
};

export const deleteProductFromCart = (orderId, itemId) => {
    return (dispatch, getState) => {
        dispatch({type: DELETE_FROM_CART_START});
        axios.delete(`/order/deleteProduct/${orderId}/${itemId}`)
            .then(resp => {
                dispatch({
                    type: DELETE_FROM_CART_END,
                });
            })
            .catch(err => {
                handleError(err);
            })
    };
}

export const placeOrder = (orderId) => {
    return (dispatch, getState) => {
        dispatch({type: PLACE_ORDER_START});
        axios.put(`/order/placeOrder/${orderId}`)
            .then(resp => {
                dispatch({
                    type: PLACE_ORDER_SUCCESS,
                });
            })
            .catch(err => {
                handleError(err);
            })
    };
}

export const selectProduct = (productId) => {
    return {
        type: SELECT_GOOD,
        payload: {
            productId: productId
        }
    };
}

export const changeQuantity = (quantity) => {
    return {
        type: CHANGE_QUANTITY,
        payload: {
            quantity: quantity
        }
    };
}

export const selectProductToDelete = (productId) => {
    return {
        type: SELECT_GOOD_TO_DELETE,
        payload: {
            productId: productId
        }
    };
}

export const cancelDeleteProduct = () => {
    return {
        type: DELETE_FROM_CART_CANCELLED
    }
}

export const openPlaceOrderDialog = () => {
    return {
        type: OPEN_PLACE_ORDER_DIALOG
    }
}

export const closePlaceOrderDialog = () => {
    return {
        type: CLOSE_PLACE_ORDER_DIALOG
    }
}
