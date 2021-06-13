import {
    ADD_TO_CART_FAIL,
    ADD_TO_CART_START,
    ADD_TO_CART_SUCCESS,
    CHANGE_QUANTITY,
    CLOSE_PLACE_ORDER_DIALOG,
    DELETE_FROM_CART_CANCELLED,
    DELETE_FROM_CART_END,
    DELETE_FROM_CART_START,
    OPEN_PLACE_ORDER_DIALOG,
    PLACE_ORDER_FAIL,
    PLACE_ORDER_START,
    PLACE_ORDER_SUCCESS,
    SELECT_GOOD,
    SELECT_GOOD_TO_DELETE
} from "../actions/cartActions";

const initialState = {
    orderId: null,
    goods: [],
    isAdding: false,
    isDeleting: false,
    isPlacing: false,
    placeOrderDialogOpen: false,
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
        case PLACE_ORDER_START:
            return {
                ...state,
                isPlacing: true
            }
        case PLACE_ORDER_SUCCESS:
            return {
                ...state,
                orderId: null,
                goods: [],
                isPlacing: false,
                placeOrderDialogOpen: false
            }
        case PLACE_ORDER_FAIL:
            return {
                ...state,
                isPlacing: false
            }
        case OPEN_PLACE_ORDER_DIALOG:
            return {
                ...state,
                placeOrderDialogOpen: true
            }
        case CLOSE_PLACE_ORDER_DIALOG:
            return {
                ...state,
                placeOrderDialogOpen: false
            }
        default:
            return state;
    }
};

export default cartReducer;
