import React from "react";
import ThemeProvider from "@material-ui/styles/ThemeProvider";
import {createMuiTheme} from "@material-ui/core";
import SignIn from "./components/auth/SignIn";

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
            <SignIn/>
        </ThemeProvider>
    );
}

export default App;
