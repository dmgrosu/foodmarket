import {LOGIN_FAIL, LOGIN_START, LOGIN_SUCCESS, SIGNUP_FAIL, SIGNUP_START, SIGNUP_SUCCESS} from "../actions/authActions";

const initialState = {
    token: null,
    isLoading: false,
    error: ""
};

const authReducer = (state = initialState, action) => {
    switch (action.type) {
        case LOGIN_START:
        case SIGNUP_START:
            return {
                ...state,
                isLoading: true,
                error: ""
            };
        case LOGIN_SUCCESS:
        case SIGNUP_SUCCESS:
            return {
                ...state,
                token: action.payload,
                isLoading: false
            };
        case LOGIN_FAIL:
        case SIGNUP_FAIL:
            return {
                ...state,
                token: null,
                isLoading: false,
                error: action.payload
            };
        default:
            return state;
    }
};

export default authReducer;
