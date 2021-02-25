import React from "react";
import ThemeProvider from "@material-ui/styles/ThemeProvider";
import {createMuiTheme} from "@material-ui/core";
import {Route, Switch, withRouter} from "react-router-dom";
import SignIn from "./components/auth/SignIn";
import SignUp from "./components/auth/SignUp";
import Home from "./components/Home";
import {ToastContainer} from "material-react-toastify";
import 'material-react-toastify/dist/ReactToastify.min.css';

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

const App = () => {
    return (
        <ThemeProvider theme={theme}>
            <Switch>
                <Route exact path='/' component={Home}/>
                <Route path='/signIn' component={SignIn}/>
                <Route path='/signUp' component={SignUp}/>
            </Switch>
            <ToastContainer position="bottom-right"
                            autoClose={5000}
            />
        </ThemeProvider>
    );
}

export default withRouter(App);
