import {
    LOGIN_FAIL,
    LOGIN_START,
    LOGIN_SUCCESS,
    LOGOUT, RESET_PASSWORD_TOKEN_EXPIRED, RESET_PASSWORD_TOKEN_VALID
} from "../actions/authActions";

const initialState = {
    token: null,
    userId: null,
    clientId: {},
    isLoading: false,
    error: null,
    isResetPasswordTokenValid: false
};

const authReducer = (state = initialState, action) => {
    switch (action.type) {
        case LOGIN_START:
            return {
                ...state,
                isLoading: true
            };
        case LOGIN_SUCCESS:
            return {
                ...state,
                token: action.payload.token,
                userId: action.payload.userId,
                isLoading: false
            };
        case LOGIN_FAIL:
            return {
                ...state,
                isLoading: false,
                error: action.payload
            };
        case LOGOUT:
            return {
                ...initialState
            };
        case RESET_PASSWORD_TOKEN_VALID:
            return {
                ...state,
                isResetPasswordTokenValid: true
            };
        case RESET_PASSWORD_TOKEN_EXPIRED:
            return {
                ...state,
                error: action.payload
            }
        default:
            return state;
    }
};

export default authReducer;
