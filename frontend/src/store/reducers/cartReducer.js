import {ADD_TO_CART_FAIL, ADD_TO_CART_START, ADD_TO_CART_SUCCESS, CHANGE_QUANTITY, DELETE_FROM_CART_CANCELLED, DELETE_FROM_CART_END, DELETE_FROM_CART_START, SELECT_GOOD, SELECT_GOOD_TO_DELETE} from "../actions/cartActions";

const initialState = {
    orderId: null,
    goods: [],
    isAdding: false,
    isDeleting: false,
    error: null,
    selectedGood: {
        id: null,
        quantity: 0,
    },
    deleteGoodId: null,
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
        case SELECT_GOOD_TO_DELETE:
            return {
                ...state,
                deleteGoodId: action.payload.goodId
            }
        case DELETE_FROM_CART_START:
            return {
                ...state,
                isDeleting: true,
            }
        case DELETE_FROM_CART_CANCELLED:
            return {
                ...state,
                deleteGoodId: null,
                isDeleting: true,
            }
        case DELETE_FROM_CART_END:
            const newGoods = state.goods.filter(good => good.goodId !== state.deleteGoodId);
            return {
                ...state,
                goods: newGoods,
                orderId: newGoods.length === 0 ? null : state.orderId,
                deleteGoodId: null,
                isDeleting: false
            }
        default:
            return state;
    }
};

export default cartReducer;
