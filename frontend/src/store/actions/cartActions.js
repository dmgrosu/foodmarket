import axios from "../../axios-instance";
import {handleError} from "./authActions";
import i18n from "../../i18n";

export const FETCH_CART_START = "FETCH_CART_START";
export const FETCH_CART_SUCCESS = "FETCH_CART_SUCCESS";
export const FETCH_CART_FAIL = "FETCH_CART_FAIL";
export const ADD_TO_CART_START = "ADD_TO_CART_START";
export const ADD_TO_CART_SUCCESS = "ADD_TO_CART_SUCCESS";
export const ADD_TO_CART_FAIL = "ADD_TO_CART_FAIL";
export const SELECT_GOOD = "SELECT_GOOD";
export const CHANGE_QUANTITY = "CHANGE_QUANTITY";
export const UPDATE_QUANTITY_START = "UPDATE_QUANTITY_START";
export const UPDATE_QUANTITY_SUCCESS = "UPDATE_QUANTITY_SUCCESS";
export const UPDATE_QUANTITY_FAIL = "UPDATE_QUANTITY_FAIL";
export const DELETE_FROM_CART_START = "DELETE_FROM_CART_START";
export const DELETE_FROM_CART_CANCELLED = "DELETE_FROM_CART_CANCELLED";
export const DELETE_FROM_CART_END = "DELETE_FROM_CART_END";
export const DELETE_FROM_CART_FAIL = "DELETE_FROM_CART_FAIL";
export const SELECT_GOOD_TO_DELETE = "SELECT_GOOD_TO_DELETE";
export const CLEAR_CART_START = "CLEAR_CART_START";
export const CLEAR_CART_SUCCESS = "CLEAR_CART_SUCCESS";
export const CLEAR_CART_FAIL = "CLEAR_CART_FAIL";
export const PLACE_ORDER_START = "PLACE_ORDER_START";
export const PLACE_ORDER_SUCCESS = "PLACE_ORDER_SUCCESS";
export const PLACE_ORDER_FAIL = "PLACE_ORDER_FAIL";
export const OPEN_PLACE_ORDER_DIALOG = "OPEN_PLACE_ORDER_DIALOG";
export const CLOSE_PLACE_ORDER_DIALOG = "CLOSE_PLACE_ORDER_DIALOG";

/**
 * The price tier every order is placed at.
 *
 * There is no tier selector in the UI and nothing on the client that says which tier a customer
 * should get, so it is a constant here. Moving it to a per-client tier is a backend change — the
 * ERP client feed carries no such field today.
 */
export const DEFAULT_PRICE_TYPE = "LOCAL";

const describeError = (err) =>
    err.response
        ? err.response.status + ": " + (err.response.data.message || err.response.statusText)
        : i18n.t('common.unknownError');

/**
 * Every cart endpoint answers with the whole cart, so the reducer never has to guess what a mutation
 * did — it replaces its state with what the server just confirmed.
 */
const toCart = (data) => ({
    orderId: data.id,
    storageId: data.storageId,
    priceType: data.priceType,
    products: data.items || [],
});

/**
 * Load the cart the server is holding for this user.
 *
 * The cart lives on the server, not in this store, so this is what makes it survive a page reload.
 */
export const fetchCart = () => {
    return (dispatch) => {
        dispatch({type: FETCH_CART_START});
        axios.get("/order/getCart")
            .then(resp => {
                dispatch({type: FETCH_CART_SUCCESS, payload: toCart(resp.data)});
            })
            .catch(err => {
                dispatch({type: FETCH_CART_FAIL, payload: {error: describeError(err)}});
                handleError(err);
            })
    };
};

export const addProductToCart = (productId, storageId, quantity) => {
    return (dispatch) => {
        dispatch({type: ADD_TO_CART_START});
        axios.post("/order/addProduct", {
            productId: productId,
            storageId: storageId,
            priceType: DEFAULT_PRICE_TYPE,
            quantity: quantity,
        })
            .then(resp => {
                dispatch({type: ADD_TO_CART_SUCCESS, payload: toCart(resp.data)});
            })
            .catch(err => {
                dispatch({type: ADD_TO_CART_FAIL, payload: {error: describeError(err)}});
                handleError(err);
            })
    };
};

export const updateCartProduct = (productId, quantity) => {
    return (dispatch) => {
        dispatch({type: UPDATE_QUANTITY_START});
        axios.put("/order/updateProduct", {productId: productId, quantity: quantity})
            .then(resp => {
                dispatch({type: UPDATE_QUANTITY_SUCCESS, payload: toCart(resp.data)});
            })
            .catch(err => {
                dispatch({type: UPDATE_QUANTITY_FAIL, payload: {error: describeError(err)}});
                handleError(err);
            })
    };
};

export const deleteProductFromCart = (productId) => {
    return (dispatch) => {
        dispatch({type: DELETE_FROM_CART_START});
        axios.delete(`/order/deleteProduct/${productId}`)
            .then(resp => {
                dispatch({type: DELETE_FROM_CART_END, payload: toCart(resp.data)});
            })
            .catch(err => {
                dispatch({type: DELETE_FROM_CART_FAIL, payload: {error: describeError(err)}});
                handleError(err);
            })
    };
};

export const clearCart = () => {
    return (dispatch) => {
        dispatch({type: CLEAR_CART_START});
        axios.delete("/order/clearCart")
            .then(() => {
                dispatch({type: CLEAR_CART_SUCCESS});
            })
            .catch(err => {
                dispatch({type: CLEAR_CART_FAIL, payload: {error: describeError(err)}});
                handleError(err);
            })
    };
};

export const placeOrder = () => {
    return (dispatch) => {
        dispatch({type: PLACE_ORDER_START});
        axios.put("/order/placeOrder")
            .then(() => {
                dispatch({type: PLACE_ORDER_SUCCESS});
            })
            .catch(err => {
                // Without this the dialog stays open on isPlacing forever.
                dispatch({type: PLACE_ORDER_FAIL, payload: {error: describeError(err)}});
                handleError(err);
            })
    };
};

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
