import React from "react";
import ThemeProvider from "@material-ui/styles/ThemeProvider";
import {createMuiTheme} from "@material-ui/core";
import {connect} from "react-redux";
import {Route, Switch, withRouter} from "react-router-dom";
import HomePage from "./components/home/HomePage";
import SignIn from "./components/auth/SignIn";
import SignUp from "./components/auth/SignUp";

const theme = createMuiTheme({
    palette: {
        primary: {
            main: '#556cd6',
        },
        secondary: {
            main: '#19857b',
        },
        error: {
            main: "#FF0000",
        },
        background: {
            default: '#fff',
        },
    }
});

const App = (props) => {

    const {token} = props.auth;

    return (
        <ThemeProvider theme={theme}>
            <Switch>
                <Route exact path='/' component={HomePage} />
                <Route path='/signIn' component={SignIn}/>
                <Route path='/signUp' component={SignUp}/>
            </Switch>
        </ThemeProvider>
    );
}

const mapStateToProps = state => ({
    auth: state.authReducer
});

export default withRouter(connect(mapStateToProps, {})(App));
