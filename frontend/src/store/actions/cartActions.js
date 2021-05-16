import axios from "axios";
import {handleError} from "./authActions";

export const ADD_TO_CART_START = "ADD_TO_CART_START";
export const ADD_TO_CART_SUCCESS = "ADD_TO_CART_SUCCESS";
export const ADD_TO_CART_FAIL = "ADD_TO_CART_FAIL";
export const SELECT_GOOD = "SELECT_GOOD";
export const CHANGE_QUANTITY = "CHANGE_QUANTITY";
export const DELETE_FROM_CART_START = "DELETE_FROM_CART_START";
export const DELETE_FROM_CART_CANCELLED = "DELETE_FROM_CART_CANCELLED";
export const DELETE_FROM_CART_END = "DELETE_FROM_CART_END";
export const SELECT_GOOD_TO_DELETE = "SELECT_GOOD_TO_DELETE";

export const addGoodToCart = (goodId, orderId, quantity) => {
    return (dispatch, getState) => {
        const {token} = getState().authReducer;
        dispatch({type: ADD_TO_CART_START});
        axios.post("/order/addGood",
            {orderId: orderId, goodId: goodId, quantity: quantity},
            {headers: {'Authorization': token}})
            .then(resp => {
                const data = resp.data;
                dispatch({
                    type: ADD_TO_CART_SUCCESS,
                    payload: {
                        orderId: data.order.id,
                        goods: data.order.goods,
                    }
                });
            })
            .catch(err => {
                dispatch({
                    type: ADD_TO_CART_FAIL,
                    payload: {
                        error: err.response ? err.response.status + ": " + err.response.data.message || err.response.statusText : "Unknown error"
                    }
                })
                handleError(err);
            })
    };
};

export const deleteGoodFromCart = (orderId, orderGoodId) => {
    return (dispatch, getState) => {
        const {token} = getState().authReducer;
        dispatch({type: DELETE_FROM_CART_START});
        axios.post("/order/deleteGood",
            {orderId: orderId, orderGoodId: orderGoodId},
            {headers: {'Authorization': token}})
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

export const selectGood = (goodId) => {
    return {
        type: SELECT_GOOD,
        payload: {
            goodId: goodId
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

export const selectGoodToDelete = (goodId) => {
    return {
        type: SELECT_GOOD_TO_DELETE,
        payload: {
            goodId: goodId
        }
    };
}

export const cancelDeleteGood = () => {
    return {
        type: DELETE_FROM_CART_CANCELLED
    }
}
