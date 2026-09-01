import {
    ADD_TO_CART_FAIL,
    ADD_TO_CART_START,
    ADD_TO_CART_SUCCESS,
    CHANGE_QUANTITY,
    CLEAR_CART_FAIL,
    CLEAR_CART_START,
    CLEAR_CART_SUCCESS,
    CLOSE_PLACE_ORDER_DIALOG,
    DELETE_FROM_CART_CANCELLED,
    DELETE_FROM_CART_END,
    DELETE_FROM_CART_FAIL,
    DELETE_FROM_CART_START,
    FETCH_CART_FAIL,
    FETCH_CART_START,
    FETCH_CART_SUCCESS,
    OPEN_PLACE_ORDER_DIALOG,
    PLACE_ORDER_FAIL,
    PLACE_ORDER_START,
    PLACE_ORDER_SUCCESS,
    SELECT_GOOD,
    SELECT_GOOD_TO_DELETE,
    UPDATE_QUANTITY_FAIL,
    UPDATE_QUANTITY_START,
    UPDATE_QUANTITY_SUCCESS
} from "../actions/cartActions";

const emptyCart = {
    orderId: null,
    storageId: null,
    priceType: null,
    products: [],
};

const initialState = {
    ...emptyCart,
    isFetching: false,
    isAdding: false,
    isUpdating: false,
    isDeleting: false,
    isPlacing: false,
    placeOrderDialogOpen: false,
    error: null,
    selectedProduct: {
        id: null,
        quantity: 1,
    },
    deleteProductId: null,
};

/**
 * Every cart mutation answers with the whole cart, so the state is replaced with what the server
 * confirmed rather than patched locally. Guessing was what let the view drift out of step with the
 * order actually stored.
 */
const cartReducer = (state = initialState, action) => {
    switch (action.type) {
        case FETCH_CART_START:
            return {
                ...state,
                isFetching: true
            };
        case FETCH_CART_SUCCESS:
            return {
                ...state,
                ...action.payload,
                isFetching: false,
                error: null
            };
        case FETCH_CART_FAIL:
            return {
                ...state,
                isFetching: false,
                error: action.payload.error
            };
        case ADD_TO_CART_START:
            return {
                ...state,
                isAdding: true
            };
        case ADD_TO_CART_SUCCESS:
            return {
                ...state,
                ...action.payload,
                isAdding: false,
                error: null,
                selectedProduct: {
                    id: null,
                    quantity: 1
                }
            };
        case ADD_TO_CART_FAIL:
            return {
                ...state,
                isAdding: false,
                error: action.payload.error,
                selectedProduct: {
                    id: null,
                    quantity: 1
                }
            };
        case SELECT_GOOD:
            return {
                ...state,
                selectedProduct: {
                    id: action.payload.productId,
                    // 1, not 0: the dialog opens on this value, so starting at 0 makes typing "3"
                    // read "03", and 0 is below the minimum the backend accepts anyway.
                    quantity: 1
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
        case UPDATE_QUANTITY_START:
            return {
                ...state,
                isUpdating: true
            }
        case UPDATE_QUANTITY_SUCCESS:
            return {
                ...state,
                ...action.payload,
                isUpdating: false,
                error: null
            }
        case UPDATE_QUANTITY_FAIL:
            return {
                ...state,
                isUpdating: false,
                error: action.payload.error
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
                isDeleting: false,
            }
        case DELETE_FROM_CART_END:
            return {
                ...state,
                ...action.payload,
                deleteProductId: null,
                isDeleting: false,
                error: null
            }
        case DELETE_FROM_CART_FAIL:
            return {
                ...state,
                deleteProductId: null,
                isDeleting: false,
                error: action.payload.error
            }
        case CLEAR_CART_START:
            return {
                ...state,
                isDeleting: true
            }
        case CLEAR_CART_SUCCESS:
            return {
                ...state,
                ...emptyCart,
                isDeleting: false,
                error: null
            }
        case CLEAR_CART_FAIL:
            return {
                ...state,
                isDeleting: false,
                error: action.payload.error
            }
        case PLACE_ORDER_START:
            return {
                ...state,
                isPlacing: true
            }
        case PLACE_ORDER_SUCCESS:
            // The placed order is no longer a cart, so the client is left holding an empty one.
            return {
                ...state,
                ...emptyCart,
                isPlacing: false,
                placeOrderDialogOpen: false,
                error: null
            }
        case PLACE_ORDER_FAIL:
            return {
                ...state,
                isPlacing: false,
                placeOrderDialogOpen: false,
                error: action.payload.error
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
