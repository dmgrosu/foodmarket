import {ADD_TO_CART_FAIL, ADD_TO_CART_START, ADD_TO_CART_SUCCESS} from "../actions/cartActions";

const initialState = {
    orderId: null,
    goods: [],
    isAdding: false,
};

const cartReducer = (state = initialState, action) => {
    switch (action.type) {
        case ADD_TO_CART_START:
            return {
                ...state,
                isAdding: true
            };
        case ADD_TO_CART_SUCCESS:
            return {
                ...state,
                goods: action.payload.goods,
                orderId: action.payload.orderId,
                isAdding: false,
            };
        case ADD_TO_CART_FAIL:
            return {
                ...state,
                isAdding: false,
            };
        default:
            return state;
    }
};

export default cartReducer;
