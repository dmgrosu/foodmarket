import axios from "axios";
import {toast} from "material-react-toastify";
import moment from "moment";

export const LOGIN_START = "LOGIN_START";
export const LOGIN_SUCCESS = "LOGIN_SUCCESS";
export const LOGIN_FAIL = "LOGIN_FAIL";
export const LOGOUT = "LOGOUT";


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

