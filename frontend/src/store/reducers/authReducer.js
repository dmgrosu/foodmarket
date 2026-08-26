import {
    LOGIN_FAIL, LOGIN_START, LOGIN_SUCCESS, LOGOUT,
    SIGNUP_START, SIGNUP_SUCCESS, SIGNUP_FAIL,
    CONFIRM_EMAIL_START, CONFIRM_EMAIL_SUCCESS, CONFIRM_EMAIL_FAIL,
    RESEND_CONFIRMATION_START, RESEND_CONFIRMATION_SUCCESS, RESEND_CONFIRMATION_FAIL
} from "../actions/authActions";

const initialState = {
    token: null,
    userId: null,
    roles: [],
    clientId: {},
    isLoading: false,
    error: null,
    // Registration/confirmation flow — separate from the login isLoading/error above so a
    // failed login and an in-flight confirmation never fight over the same flag.
    signUpEmail: null,
    confirmationEmailSent: null,
    isConfirming: false,
    confirmed: false,
    confirmError: null
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
                roles: action.payload.roles,
                isLoading: false
            };
        case LOGIN_FAIL:
            return {
                ...state,
                isLoading: false,
                error: action.payload
            };
        case SIGNUP_START:
            return {
                ...state,
                isLoading: true
            };
        case SIGNUP_SUCCESS:
            return {
                ...state,
                isLoading: false,
                signUpEmail: action.payload.email,
                confirmationEmailSent: action.payload.confirmationEmailSent
            };
        case SIGNUP_FAIL:
            return {
                ...state,
                isLoading: false,
                error: action.payload
            };
        case CONFIRM_EMAIL_START:
            return {
                ...state,
                isConfirming: true,
                confirmed: false,
                confirmError: null
            };
        case CONFIRM_EMAIL_SUCCESS:
            return {
                ...state,
                isConfirming: false,
                confirmed: true
            };
        case CONFIRM_EMAIL_FAIL:
            return {
                ...state,
                isConfirming: false,
                confirmed: false,
                confirmError: action.payload
            };
        case RESEND_CONFIRMATION_START:
            return {
                ...state,
                isLoading: true
            };
        case RESEND_CONFIRMATION_SUCCESS:
            return {
                ...state,
                isLoading: false,
                signUpEmail: action.payload.email,
                confirmationEmailSent: action.payload.confirmationEmailSent
            };
        case RESEND_CONFIRMATION_FAIL:
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
