import axios from "axios";
import {toast} from "material-react-toastify";
import moment from "moment";

export const LOGIN_START = "LOGIN_START";
export const LOGIN_SUCCESS = "LOGIN_SUCCESS";
export const LOGIN_FAIL = "LOGIN_FAIL";
export const LOGOUT = "LOGOUT";
export const RESET_PASSWORD_TOKEN_VALID = "TOKEN_VALID";
export const RESET_PASSWORD_TOKEN_EXPIRED = "RESET_PASSWORD_TOKEN_EXPIRED";
export const RESET_PASSWORD_START = "RESET_PASSWORD_START";
export const RESET_PASSWORD_SUCCESS = "RESET_PASSWORD_SUCCESS";
export const RESET_PASSWORD_FAIL = "RESET_PASSWORD_FAIL";


export const loginStart = (email, password) => {
    return dispatch => {
        dispatch({type: LOGIN_START});
        axios.post("/auth/login", {email: email, password: password})
            .then(resp => {
                const data = resp.data;
                storeAuthData(data);
                dispatch(loginSuccess(data.token, data.user.id));
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

export const loginSuccess = (token, userId) => {
    return {
        type: LOGIN_SUCCESS,
        payload: {
            token: token,
            userId: userId
        }
    };
}

export const signUpStart = (email, password, clientId) => {
    return dispatch => {
        dispatch({type: LOGIN_START});
        axios.post("/auth/register", {email: email, password: password, clientId: clientId})
            .then(resp => {
                const data = resp.data;
                storeAuthData(data);
                dispatch(loginSuccess(data.token, data.user.id));
                dispatch(checkAuthTimeout(data.tokenTtl))
            })
            .catch(err => {
                dispatch({
                    type: LOGIN_FAIL,
                    payload: err.response.data
                })
                handleError(err);
            })
    };
};

export const resetPasswordStart = (email) => {
    return dispatch => {
        dispatch({type: RESET_PASSWORD_START});
        axios.post("/auth/resetPassword", {email: email});
    }
}

export const requestResetPasswordTokenValidation = (token) => {
    return dispatch => {
        axios.post("/auth/validateResetPasswordToken", {token: token})
            .then(resp => {
                dispatch({
                    type: RESET_PASSWORD_TOKEN_VALID,
                })
            })
            .catch(err => {
                dispatch({
                    type: RESET_PASSWORD_TOKEN_EXPIRED,
                    payload: err.response.data
                })
                handleError(err);
            })
    }
}

export const setNewPassword =  (token, newPassword) => {
    return dispatch => {
        axios.post("/auth/newPassword", {token: token, newPassword: newPassword})
            .then(resp => {
                dispatch({
                    type: RESET_PASSWORD_SUCCESS,
                });
            })
            .catch(err => {
                dispatch({
                    RESET_PASSWORD_FAIL,
                    payload: err.response.data
                });
                handleError(err);
            });
    }
}


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
        if (storedToken) {
            const validUntilStr = localStorage.getItem('validUntil');
            if (validUntilStr) {
                const validUntil = moment(Number(validUntilStr));
                if (moment().isBefore(validUntil)) {
                    const userId = localStorage.getItem('userId');
                    dispatch(loginSuccess(storedToken, userId));
                    dispatch(checkAuthTimeout(validUntil.diff(moment(), 'second')));
                }
            }
        }
    }
}

export const logout = () => {
    removeAuthData();
    return {type: LOGOUT}
}

export const handleError = (err) => {
    const errorMessage = err.response ? err.response.status + ": " + err.response.data.message || err.response.statusText : "Unknown error";
    toast.error(errorMessage);
}

const storeAuthData = (authData) => {
    localStorage.setItem('token', authData.token);
    localStorage.setItem('userId', authData.user.id);
    localStorage.setItem('validUntil', moment().add(Number(authData.tokenTtl), 'second').valueOf().toString());
}

const removeAuthData = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('validUntil');
    localStorage.removeItem('userId');
}

