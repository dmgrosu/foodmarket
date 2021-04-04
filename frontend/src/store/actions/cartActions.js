import axios from "axios";
import {handleError} from "./authActions";

export const ADD_TO_CART_START = "ADD_TO_CART_START";
export const ADD_TO_CART_SUCCESS = "ADD_TO_CART_SUCCESS";
export const ADD_TO_CART_FAIL = "ADD_TO_CART_FAIL";

export const addToCartStart = (goodId, orderId, quantity) => {
    return dispatch => {
        dispatch({type: ADD_TO_CART_START});
        axios.post("/order/addGood", {orderId: orderId, goodId: goodId, quantity: quantity})
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
                handleError(err);
            })
    };
};
