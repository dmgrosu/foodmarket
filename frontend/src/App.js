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
import Sidebar from "./components/navigation/Sidebar";
import Container from "@material-ui/core/Container";

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
        <Route path='/signIn' component={SignIn} key={2}/>,
        <Route path='/signUp' component={SignUp} key={3}/>,
        <Route exact path='/' component={Home} key={1}/>,
        <Redirect to='/' key={4}/>,
    ];

    if (isAuthenticated) {
        axios.interceptors.request.use(request => {
            request.headers.common['Authorization'] = token;
            return request;
        });
        routes = [
            <Route path='/goods' component={Home} key={2}/>,
            <Route path='/orders' component={Home} key={3}/>,
            <Route path='/profile' component={Home} key={3}/>,
            <Route exact path='/' component={Home} key={1}/>,
            <Redirect to='/' key={4}/>
        ];
    }

    return (
        <ThemeProvider theme={theme}>
            <Navbar/>
            <Sidebar isAuthenticated={isAuthenticated}/>
            <Container component="main"
                       maxWidth="xs"
                       style={{
                           flexGrow: 1,
                           padding: theme.spacing(3),
                       }}
            >
                <Switch>
                    {routes}
                </Switch>
            </Container>
            <ToastContainer position="bottom-right"
                            autoClose={5000}
            />
        </ThemeProvider>
    );
}

const mapStateToProps = state => ({
    auth: state.authReducer,
});

export default withRouter(connect(mapStateToProps, null)(App));
