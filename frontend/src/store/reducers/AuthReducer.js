import {LOGIN_FAIL, LOGIN_START, LOGIN_SUCCESS} from "../actions/AuthActions";

const initialState = {
    token: null,
    isLoading: false,
    error: ""
};

const authReducer = (state = initialState, action) => {
    switch (action.type) {
        case LOGIN_START:
            return {
                ...state,
                isLoading: true,
                error: ""
            };
        case LOGIN_SUCCESS:
            return {
                ...state,
                token: action.payload,
                isLoading: false
            };
        case LOGIN_FAIL:
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
