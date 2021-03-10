import React from "react";
import ThemeProvider from "@material-ui/styles/ThemeProvider";
import {createMuiTheme} from "@material-ui/core";
import {Redirect, Route, Switch, withRouter} from "react-router-dom";
import SignIn from "./components/auth/SignIn";
import SignUp from "./components/auth/SignUp";
import Home from "./components/Home";
import {ToastContainer} from "material-react-toastify";
import 'material-react-toastify/dist/ReactToastify.min.css';
import {connect} from "react-redux";
import axios from "axios";
import Navbar from "./components/navigation/Navbar";
import Goods from "./components/goods/Goods";
import Profile from "./components/auth/Profile";
import CssBaseline from "@material-ui/core/CssBaseline";
import Orders from "./components/orders/Orders";
import {authCheckState} from "./store/actions/authActions";

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
    const isAuthenticated = token !== null;

    let routes = [
        <Route path='/signIn' component={SignIn} key={1}/>,
        <Route path='/signUp' component={SignUp} key={2}/>,
        <Route exact path='/' component={Home} key={3}/>,
    ];

    if (isAuthenticated) {
        routes.push(
            <Route path='/goods' component={Goods} key={4}/>,
            <Route path='/orders' component={Orders} key={5}/>,
            <Route path='/profile' component={Profile} key={6}/>,
        );
    } else {
        routes.push(<Redirect to="/" key={0}/>)
        props.authCheckState();
    }

    axios.interceptors.request.use(request => {
        request.headers.common['Authorization'] = token;
        return request;
    });

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline/>
            <Navbar/>
            <Switch>
                {routes}
            </Switch>
            <ToastContainer position="bottom-right"
                            autoClose={5000}
            />
        </ThemeProvider>
    );
}

const mapStateToProps = state => ({
    auth: state.authReducer,
});

export default withRouter(connect(mapStateToProps, {
    authCheckState
})(App));
