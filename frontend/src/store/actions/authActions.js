import axios from "axios";

export const LOGIN_START = "LOGIN_START";
export const LOGIN_SUCCESS = "LOGIN_SUCCESS";
export const LOGIN_FAIL = "LOGIN_FAIL";
export const SIGNUP_START = "SIGNUP_START";
export const SIGNUP_SUCCESS = "SIGNUP_SUCCESS";
export const SIGNUP_FAIL = "SIGNUP_FAIL";


export const loginStart = (email, password) => {
    return dispatch => {
        dispatch(() => ({type: LOGIN_START}));
        axios.post("/auth/login", {email: email, password: password})
            .then(resp => {
                dispatch(() => ({
                    type: LOGIN_SUCCESS,
                    token: resp.data
                }));
            })
            .catch(err => {
                dispatch(() => ({
                    type: LOGIN_FAIL,
                    error: err.data
                }))
            })
    };
};

export const signUpStart = (firstName, lastName, email, password) => {
    return dispatch => {
        dispatch(() => ({type: SIGNUP_START}));
        console.log(email, password);
        axios.post("/auth/register", {email: email, password: password})
            .then(resp => {
                dispatch(() => ({
                    type: SIGNUP_SUCCESS,
                    token: resp.data
                }));
            })
            .catch(err => {
                dispatch(() => ({
                    type: SIGNUP_FAIL,
                    error: err.data
                }))
            })
    };
};

