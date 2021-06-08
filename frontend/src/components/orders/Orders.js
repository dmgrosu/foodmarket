import React, {Component} from 'react';
import {connect} from "react-redux";
import {withStyles} from "@material-ui/styles";

const styles = theme => ({
    root: {
        flexGrow: 1,
        padding: theme.spacing(2),
    }
});

class Orders extends Component {

    state = {
        orders: [],
        isLoading: false
    }

    render() {
        return (
            <div>
                {"Inside orders component"}
            </div>
        )
    }
}

const mapStateToProps = state => ({
    auth: state.authReducer
});

export default connect(mapStateToProps, {})(withStyles(styles)(Orders));
