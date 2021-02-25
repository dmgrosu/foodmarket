import axios from "axios";
import {toast} from "material-react-toastify";

export const LOGIN_START = "LOGIN_START";
export const LOGIN_SUCCESS = "LOGIN_SUCCESS";
export const LOGIN_FAIL = "LOGIN_FAIL";
export const SIGNUP_START = "SIGNUP_START";
export const SIGNUP_SUCCESS = "SIGNUP_SUCCESS";
export const SIGNUP_FAIL = "SIGNUP_FAIL";


export const loginStart = (email, password) => {
    return dispatch => {
        dispatch({type: LOGIN_START});
        axios.post("/auth/login", {email: email, password: password})
            .then(resp => {
                dispatch({
                    type: LOGIN_SUCCESS,
                    payload: resp.data
                });
            })
            .catch(err => {
                dispatch({
                    type: LOGIN_FAIL,
                    payload: err.response.data
                });
                toast.error(err.response.status + ": " + err.response.statusText || err.response.data.message);
            })
    };
};

export const signUpStart = (email, password, clientId) => {
    return dispatch => {
        dispatch({type: SIGNUP_START});
        axios.post("/auth/register", {email: email, password: password, clientId: clientId})
            .then(resp => {
                dispatch({
                    type: SIGNUP_SUCCESS,
                    payload: resp.data
                });
            })
            .catch(err => {
                dispatch({
                    type: SIGNUP_FAIL,
                    payload: err.data
                })
            })
    };
};

