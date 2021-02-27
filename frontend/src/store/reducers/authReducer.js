import {LOGIN_FAIL, LOGIN_START, LOGIN_SUCCESS, LOGOUT} from "../actions/authActions";

const initialState = {
    token: null,
    userId: null,
    userEmail: null,
    isLoading: false,
    error: null
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
                userId: action.payload.user.id,
                userEmail: action.payload.user.email,
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
            }
        default:
            return state;
    }
};

export default authReducer;
