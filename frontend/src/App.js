import React from "react";
import ThemeProvider from "@material-ui/styles/ThemeProvider";
import {createMuiTheme} from "@material-ui/core";
import {Route, Switch, withRouter} from "react-router-dom";
import SignIn from "./components/auth/SignIn";
import SignUp from "./components/auth/SignUp";
import ConfirmEmail from "./components/auth/ConfirmEmail";
import Home from "./components/home/Home";
import {ToastContainer} from "material-react-toastify";
import 'material-react-toastify/dist/ReactToastify.min.css';
import {connect} from "react-redux";
import Navbar from "./components/navigation/Navbar";
import Products from "./components/products/Products";
import Profile from "./components/auth/Profile";
import CssBaseline from "@material-ui/core/CssBaseline";
import Orders from "./components/orders/Orders";
import {authCheckState} from "./store/actions/authActions";
import Cart from "./components/orders/Cart";
import AdminDashboard from "./components/admin/AdminDashboard";
import {isAdmin} from "./store/selectors/authSelectors";

const theme = createMuiTheme({
    palette: {
        primary: {
            main: '#32425a',
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

    if (!isAuthenticated) {
        props.authCheckState();
    }

    let routes = [
        <Route path='/signIn' component={SignIn} key={1}/>,
        <Route path='/signUp' component={SignUp} key={2}/>,
        <Route exact path='/' component={Home} key={3}/>,
        <Route path='/confirmEmail' component={ConfirmEmail} key={9}/>,
    ];

    if (isAuthenticated) {
        routes.push(
            <Route path='/products' component={Products} key={4}/>,
            <Route path='/orders' component={Orders} key={5}/>,
            <Route path='/profile' component={Profile} key={6}/>,
            <Route path='/cart' component={Cart} key={7}/>,
        );
    }

    if (isAuthenticated && props.userIsAdmin) {
        routes.push(
            <Route path='/admin' component={AdminDashboard} key={8}/>,
        );
    }

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
    userIsAdmin: isAdmin(state),
});

export default withRouter(connect(mapStateToProps, {
    authCheckState
})(App));
