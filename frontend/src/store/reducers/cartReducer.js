import {ADD_TO_CART_FAIL, ADD_TO_CART_START, ADD_TO_CART_SUCCESS, CHANGE_QUANTITY, SELECT_GOOD} from "../actions/cartActions";

const initialState = {
    orderId: null,
    goods: [],
    isAdding: false,
    error: null,
    selectedGood: {
        id: null,
        quantity: 0,
    },
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
                selectedGood: {
                    id: null,
                    quantity: 0
                }
            };
        case ADD_TO_CART_FAIL:
            return {
                ...state,
                isAdding: false,
                error: action.payload.error
            };
        case SELECT_GOOD:
            return {
                ...state,
                selectedGood: {
                    id: action.payload.goodId,
                    quantity: 0
                }
            }
        case CHANGE_QUANTITY:
            return {
                ...state,
                selectedGood: {
                    ...state.selectedGood,
                    quantity: action.payload.quantity
                }
            }
        default:
            return state;
    }
};

export default cartReducer;
