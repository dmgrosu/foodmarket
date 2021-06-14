import React, {Component} from 'react';
import {connect} from "react-redux";
import {withStyles} from "@material-ui/styles";
import {Grid} from "@material-ui/core";
import {Redirect} from "react-router-dom";
import OrdersList from "./OrdersList";
import axios from "axios";
import {handleError} from "../../store/actions/authActions";
import moment from "moment";

const styles = theme => ({
    root: {
        flexGrow: 1,
        padding: theme.spacing(2),
        maxWidth: 1200
    }
});

class Orders extends Component {

    state = {
        orders: [],
        isLoadingOrders: false,
        pagination: {
            currentPage: 0,
            pageSize: 10,
            totalCount: 0
        }
    }

    fetchOrders = (currentPage, pageSize) => {
        this.setState({
            isLoadingOrders: true
        });
        axios.post("/order/getOrdersByPeriod", {
            dateFrom: moment().utc().startOf("month").valueOf(),
            dateTo: moment().utc().valueOf(),
            clientId: 0,
            pagination: {
                pageNo: currentPage,
                perPage: pageSize
            },
            sorting: {
                columnName: 'createdAt',
                direction: 0
            }
        }, {
            headers: {'Authorization': this.props.auth.token}
        })
            .then(resp => {
                const {orders, pagination} = resp.data;
                this.setState({
                    orders: orders,
                    pagination: {
                        currentPage: pagination.pageNo || 0,
                        pageSize: pagination.perPage,
                        totalCount: Number(pagination.totalCount)
                    },
                    isLoadingOrders: false
                })
            })
            .catch(err => {
                handleError(err);
                this.setState({
                    isLoadingOrders: false
                })
            });
    }

    componentDidMount() {
        const {pagination} = this.state;
        this.fetchOrders(pagination.currentPage, pagination.pageSize);
    }

    changePageSize = (event) => {
        const {pagination} = this.state;
        this.fetchOrders(pagination.currentPage, parseInt(event.target.value, 10));
    }

    changePage = (event, newPage) => {
        const {pagination} = this.state;
        this.fetchOrders(newPage, pagination.pageSize);
    }

    render() {

        const {auth, classes} = this.props;
        const isAuthorized = auth.token !== null;
        const {orders, isLoadingOrders, pagination} = this.state;

        return (
            <Grid container className={classes.root}>
                {!isAuthorized && <Redirect to="/signIn"/>}
                <Grid item sm={12}>
                    <OrdersList orders={orders}
                                isFetching={isLoadingOrders}
                                pagination={pagination}
                                changePageSize={this.changePageSize}
                                changeCurrentPage={this.changePage}
                    />
                </Grid>
            </Grid>
        )
    }
}

const mapStateToProps = state => ({
    auth: state.authReducer
});

export default connect(mapStateToProps, {})(withStyles(styles)(Orders));
