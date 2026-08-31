import {
    LOGIN_FAIL, LOGIN_START, LOGIN_SUCCESS, LOGOUT,
    SIGNUP_START, SIGNUP_SUCCESS, SIGNUP_FAIL,
    CONFIRM_EMAIL_START, CONFIRM_EMAIL_SUCCESS, CONFIRM_EMAIL_FAIL,
    RESEND_CONFIRMATION_START, RESEND_CONFIRMATION_SUCCESS, RESEND_CONFIRMATION_FAIL,
    FORGOT_PASSWORD_START, FORGOT_PASSWORD_SUCCESS, FORGOT_PASSWORD_FAIL,
    RESET_PASSWORD_START, RESET_PASSWORD_SUCCESS, RESET_PASSWORD_FAIL
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
    confirmError: null,
    // Password reset flow — its own flags for the same reason: a reset in flight must not look like
    // a login in flight.
    resetRequestedFor: null,
    isResetting: false,
    resetDone: false,
    resetError: null
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
        case FORGOT_PASSWORD_START:
            return {
                ...state,
                isLoading: true,
                resetRequestedFor: null
            };
        case FORGOT_PASSWORD_SUCCESS:
            return {
                ...state,
                isLoading: false,
                resetRequestedFor: action.payload.email
            };
        case FORGOT_PASSWORD_FAIL:
            return {
                ...state,
                isLoading: false,
                error: action.payload
            };
        case RESET_PASSWORD_START:
            return {
                ...state,
                isResetting: true,
                resetError: null
            };
        case RESET_PASSWORD_SUCCESS:
            return {
                ...state,
                isResetting: false,
                resetDone: true
            };
        case RESET_PASSWORD_FAIL:
            return {
                ...state,
                isResetting: false,
                resetError: action.payload
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
