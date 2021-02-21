import React from 'react';
import Navbar from "./Navbar";
import {Paper} from "@material-ui/core";
import {connect} from "react-redux";

const Home = (props) => {

    const {token} = props.auth;

    return (
        <div>
            <Navbar authorized={!!token}/>
            <Paper>
                Here will be implemented home page
            </Paper>
        </div>
    )
}

const mapStateToProps = state => ({
    auth: state.authReducer
});

export default connect(mapStateToProps, {})(Home);
