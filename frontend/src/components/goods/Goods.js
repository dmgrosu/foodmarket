import React, {Component} from 'react';
import {Container, Paper} from "@material-ui/core";
import {connect} from "react-redux";
import {withStyles} from "@material-ui/styles";
import {Redirect} from "react-router-dom";
import Filter from "./Filter";
import axios from "axios";

const styles = theme => ({
    root: {
        flexGrow: 1,
        padding: theme.spacing(2),
    }
});

class Goods extends Component {

    state = {
        filter: {
            brandId: 0,
            name: "",
        },
        allBrands: [],
    }

    changeFilter = (event, field) => {
        this.setState(state => ({
            filter: {
                ...state.filter,
                [field]: event.target.value,
            },
        }))
    }

    performSearch = () => {

    }

    fetchBrands = () => {
        axios.get("/brand/getAll", {headers: {'Authorization': this.props.auth.token}})
            .then((resp) => {
                const {brands} = resp.data || [];
                this.setState({
                    allBrands: brands.filter(brand => brand.id && brand.name),
                })
            })
            .catch((err) => {
                console.log(err);
            });
    }

    componentDidMount() {
        this.fetchBrands();
    }

    render() {

        const {auth, classes} = this.props;
        const isAuthorized = auth.token !== null;
        const {filter, allBrands} = this.state;

        return (
            <Container spacing={2} className={classes.root}>
                {!isAuthorized && <Redirect to="/signIn"/>}
                <Paper>
                    <Filter brands={allBrands}
                            brandId={filter.brandId}
                            name={filter.name}
                            changeFilter={this.changeFilter}
                            search={this.performSearch}
                    />
                </Paper>
            </Container>
        )
    }
}

const mapStateToProps = state => ({
    auth: state.authReducer
});

export default connect(mapStateToProps, {})(withStyles(styles)(Goods));
