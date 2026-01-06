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
    products: [],
    isAdding: false,
    isDeleting: false,
    isPlacing: false,
    placeOrderDialogOpen: false,
    error: null,
    selectedProduct: {
        id: null,
        quantity: 0,
    },
    deleteProductId: null,
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
                products: action.payload.products,
                orderId: action.payload.orderId,
                isAdding: false,
                selectedProduct: {
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
                selectedProduct: {
                    id: action.payload.productId,
                    quantity: 0
                }
            }
        case CHANGE_QUANTITY:
            return {
                ...state,
                selectedProduct: {
                    ...state.selectedProduct,
                    quantity: action.payload.quantity
                }
            }
        case SELECT_GOOD_TO_DELETE:
            return {
                ...state,
                deleteProductId: action.payload.productId
            }
        case DELETE_FROM_CART_START:
            return {
                ...state,
                isDeleting: true,
            }
        case DELETE_FROM_CART_CANCELLED:
            return {
                ...state,
                deleteProductId: null,
                isDeleting: true,
            }
        case DELETE_FROM_CART_END:
            const newProducts = state.products.filter(product => product.productId !== state.deleteProductId);
            return {
                ...state,
                products: newProducts,
                orderId: newProducts.length === 0 ? null : state.orderId,
                deleteProductId: null,
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
                products: [],
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
