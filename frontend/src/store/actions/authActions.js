import axios from "../../axios-instance";
import {toast} from "material-react-toastify";
import moment from "moment";
import i18n from "../../i18n";

export const LOGIN_START = "LOGIN_START";
export const LOGIN_SUCCESS = "LOGIN_SUCCESS";
export const LOGIN_FAIL = "LOGIN_FAIL";
export const LOGOUT = "LOGOUT";
export const SIGNUP_START = "SIGNUP_START";
export const SIGNUP_SUCCESS = "SIGNUP_SUCCESS";
export const SIGNUP_FAIL = "SIGNUP_FAIL";
export const CONFIRM_EMAIL_START = "CONFIRM_EMAIL_START";
export const CONFIRM_EMAIL_SUCCESS = "CONFIRM_EMAIL_SUCCESS";
export const CONFIRM_EMAIL_FAIL = "CONFIRM_EMAIL_FAIL";
export const RESEND_CONFIRMATION_START = "RESEND_CONFIRMATION_START";
export const RESEND_CONFIRMATION_SUCCESS = "RESEND_CONFIRMATION_SUCCESS";
export const RESEND_CONFIRMATION_FAIL = "RESEND_CONFIRMATION_FAIL";


export const loginStart = (email, password) => {
    return dispatch => {
        dispatch({type: LOGIN_START});
        axios.post("/auth/login", {email: email, password: password})
            .then(resp => {
                const data = resp.data;
                storeAuthData(data);
                dispatch(loginSuccess(data.token, data.user.id, data.user.roles));
                dispatch(checkAuthTimeout(data.tokenTtl))
            })
            .catch(err => {
                const errorData = err.response ? err.response.data : "Unknown error";
                dispatch({
                    type: LOGIN_FAIL,
                    payload: errorData
                });
                handleError(err);
            })
    };
};

export const loginSuccess = (token, userId, roles) => {
    return {
        type: LOGIN_SUCCESS,
        payload: {
            token: token,
            userId: userId,
            roles: roles || []
        }
    };
}

// Registration does not log the user in — the account starts PENDING_CONFIRMATION and only
// becomes usable once the magic-link email is confirmed. SIGNUP_SUCCESS carries the email (for the
// "check your inbox" page) and whether the confirmation mail actually went out.
export const signUpStart = (email, password, clientId) => {
    return dispatch => {
        dispatch({type: SIGNUP_START});
        // The language is stored on the user, so later emails (a resend an admin triggers, an
        // activation notice) reach them in their own language rather than the sender's.
        axios.post("/auth/register", {
            email: email,
            password: password,
            clientId: clientId,
            language: i18n.resolvedLanguage
        })
            .then(resp => {
                const data = resp.data;
                dispatch({
                    type: SIGNUP_SUCCESS,
                    payload: {email: data.email, confirmationEmailSent: data.confirmationEmailSent}
                });
            })
            .catch(err => {
                dispatch({
                    type: SIGNUP_FAIL,
                    payload: err.response.data
                })
                handleError(err);
            })
    };
};

export const confirmEmail = (confirmationToken) => {
    return dispatch => {
        dispatch({type: CONFIRM_EMAIL_START});
        axios.post("/auth/confirmEmail", {confirmationToken: confirmationToken})
            .then(resp => {
                const data = resp.data;
                dispatch({type: CONFIRM_EMAIL_SUCCESS, payload: data});
                if (data.state === ACTIVE_STATE) {
                    // Already approved, so the token works right now: sign them straight in.
                    storeSession(data.token, data.tokenTtl);
                    dispatch(loginSuccess(data.token, null, []));
                    dispatch(checkAuthTimeout(data.tokenTtl));
                } else {
                    // Confirmed but not approved yet. The token is inert server-side, so treating it
                    // as a session would render an authenticated UI where every call 401s. Park it
                    // instead and pick it up once an administrator approves — see authCheckState.
                    storePendingSession(data.token, data.tokenTtl);
                }
            })
            .catch(err => {
                dispatch({
                    type: CONFIRM_EMAIL_FAIL,
                    payload: err.response ? err.response.data : null
                });
            })
    };
};

export const resendConfirmation = (email) => {
    return dispatch => {
        dispatch({type: RESEND_CONFIRMATION_START});
        axios.post("/auth/resendConfirmation", {email: email})
            .then(resp => {
                const data = resp.data;
                dispatch({
                    type: RESEND_CONFIRMATION_SUCCESS,
                    payload: {email: data.email, confirmationEmailSent: data.confirmationEmailSent}
                });
            })
            .catch(err => {
                dispatch({
                    type: RESEND_CONFIRMATION_FAIL,
                    payload: err.response ? err.response.data : null
                });
                handleError(err);
            })
    };
};

export const checkAuthTimeout = (seconds) => {
    return dispatch => {
        setTimeout(() => {
            dispatch(logout());
        }, seconds * 1000);
    }
}

export const authCheckState = () => {
    return dispatch => {
        const storedToken = localStorage.getItem('token');
        const validUntilStr = localStorage.getItem('validUntil');
        if (storedToken && validUntilStr) {
            const validUntil = moment(Number(validUntilStr));
            if (moment().isBefore(validUntil)) {
                const userId = localStorage.getItem('userId');
                dispatch(loginSuccess(storedToken, userId, readStoredRoles()));
                dispatch(checkAuthTimeout(validUntil.diff(moment(), 'second')));
                return;
            }
        }
        promotePendingSession(dispatch);
    }
}

// authCheckState runs on every render while signed out, so the probe is guarded to fire at most
// once per page load rather than on every pass.
let pendingSessionProbed = false;

/**
 * A session parked at email confirmation is inert until an administrator approves the account.
 * There is no way to know that happened other than trying it, so probe an authenticated endpoint
 * once: a 200 means the account went ACTIVE and the user is signed in without retyping anything.
 */
const promotePendingSession = (dispatch) => {
    if (pendingSessionProbed) {
        return;
    }
    pendingSessionProbed = true;

    const pendingToken = localStorage.getItem(PENDING_TOKEN_KEY);
    const pendingValidUntilStr = localStorage.getItem(PENDING_VALID_UNTIL_KEY);
    if (!pendingToken || !pendingValidUntilStr) {
        return;
    }

    const pendingValidUntil = moment(Number(pendingValidUntilStr));
    if (!moment().isBefore(pendingValidUntil)) {
        clearPendingSession();
        return;
    }

    axios.get("/storage", {headers: {Authorization: pendingToken}})
        .then(() => {
            const secondsLeft = pendingValidUntil.diff(moment(), 'second');
            clearPendingSession();
            storeSession(pendingToken, secondsLeft);
            dispatch(loginSuccess(pendingToken, null, []));
            dispatch(checkAuthTimeout(secondsLeft));
        })
        .catch(() => {
            // Still awaiting approval (or the token expired). Leave it parked and stay signed out.
        });
}

export const logout = () => {
    removeAuthData();
    return {type: LOGOUT}
}

export const handleError = (err) => {
    console.log(err);
    const errorMessage = err.response ? err.response.status + ": " + err.response.data.message || err.response.statusText : i18n.t('common.unknownError');
    toast.error(errorMessage);
}

const ACTIVE_STATE = 'ACTIVE';
const PENDING_TOKEN_KEY = 'pendingToken';
const PENDING_VALID_UNTIL_KEY = 'pendingValidUntil';

/**
 * Store a usable session. Unlike storeAuthData this takes the token directly, because the confirm
 * response carries no user object — only email, state, token and ttl.
 */
const storeSession = (token, ttlSeconds) => {
    localStorage.setItem('token', token);
    localStorage.setItem('validUntil', moment().add(Number(ttlSeconds), 'second').valueOf().toString());
    localStorage.setItem('roles', JSON.stringify([]));
}

const storePendingSession = (token, ttlSeconds) => {
    localStorage.setItem(PENDING_TOKEN_KEY, token);
    localStorage.setItem(PENDING_VALID_UNTIL_KEY,
        moment().add(Number(ttlSeconds), 'second').valueOf().toString());
}

const clearPendingSession = () => {
    localStorage.removeItem(PENDING_TOKEN_KEY);
    localStorage.removeItem(PENDING_VALID_UNTIL_KEY);
}

const storeAuthData = (authData) => {
    localStorage.setItem('token', authData.token);
    localStorage.setItem('userId', authData.user.id);
    localStorage.setItem('validUntil', moment().add(Number(authData.tokenTtl), 'second').valueOf().toString());
    localStorage.setItem('roles', JSON.stringify(authData.user.roles || []));
}

const readStoredRoles = () => {
    try {
        const stored = localStorage.getItem('roles');
        return stored ? JSON.parse(stored) : [];
    } catch (e) {
        // A corrupted entry must not block sign-in; fall back to no roles.
        return [];
    }
}

const removeAuthData = () => {
    clearPendingSession();
    localStorage.removeItem('token');
    localStorage.removeItem('validUntil');
    localStorage.removeItem('userId');
    localStorage.removeItem('roles');
}

