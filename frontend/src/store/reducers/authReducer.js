import {LOGIN_FAIL, LOGIN_START, LOGIN_SUCCESS, SIGNUP_FAIL, SIGNUP_START, SIGNUP_SUCCESS} from "../actions/authActions";

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
        case SIGNUP_START:
            return {
                ...state,
                isLoading: true,
                error: null
            };
        case LOGIN_SUCCESS:
        case SIGNUP_SUCCESS:
            return {
                ...state,
                token: action.payload.token,
                userId: action.payload.user.id,
                userEmail: action.payload.user.email,
                isLoading: false
            };
        case LOGIN_FAIL:
        case SIGNUP_FAIL:
            return {
                ...state,
                isLoading: false,
                error: action.payload
            };
        default:
            return state;
    }
};

export default authReducer;
