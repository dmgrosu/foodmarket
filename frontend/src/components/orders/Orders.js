import React, {Component} from 'react';
import {connect} from "react-redux";
import {withStyles} from "@material-ui/styles";
import {Grid} from "@material-ui/core";
import {Redirect} from "react-router-dom";
import OrdersList from "./OrdersList";
import axios from "axios";
import {handleError} from "../../store/actions/authActions";
import moment from "moment";
import DateRange from "./DateRange";
import {toast} from "material-react-toastify";

const styles = theme => ({
    root: {
        flexGrow: 1,
        padding: theme.spacing(2),
        maxWidth: 1200,
        margin: 'auto',
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
        },
        dateFrom: moment().utc().startOf("month"),
        dateTo: moment().utc(),
    }

    fetchOrders = (currentPage, pageSize, dateFrom, dateTo) => {
        this.setState({
            isLoadingOrders: true
        });
        axios.post("/order/getOrdersByPeriod", {
            dateFrom: dateFrom.valueOf(),
            dateTo: dateTo.valueOf(),
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
        const {pagination, dateFrom, dateTo} = this.state;
        this.fetchOrders(pagination.currentPage, pagination.pageSize, dateFrom, dateTo);
    }

    changePageSize = (event) => {
        const {pagination, dateFrom, dateTo} = this.state;
        this.fetchOrders(pagination.currentPage, parseInt(event.target.value, 10), dateFrom, dateTo);
    }

    changePage = (event, newPage) => {
        const {pagination, dateFrom, dateTo} = this.state;
        this.fetchOrders(newPage, pagination.pageSize, dateFrom, dateTo);
    }

    changeDateFrom = (newDate) => {
        if (newDate > this.state.dateTo) {
            toast.warning("Invalid date range: start date is grater than end date");
            return;
        }
        this.setState({
            dateFrom: newDate
        });
        const {pagination, dateTo} = this.state;
        this.fetchOrders(pagination.currentPage, pagination.pageSize, newDate, dateTo);
    }

    changeDateTo = (newDate) => {
        if (newDate < this.state.dateFrom) {
            toast.warning("Invalid date range: end date is less than start date");
            return;
        }
        this.setState({
            dateTo: newDate
        });
        const {pagination, dateFrom} = this.state;
        this.fetchOrders(pagination.currentPage, pagination.pageSize, dateFrom, newDate);
    }

    render() {

        const {auth, classes} = this.props;
        const isAuthorized = auth.token !== null;
        const {orders, isLoadingOrders, pagination, dateFrom, dateTo} = this.state;

        return (
            <Grid container className={classes.root}>
                {!isAuthorized && <Redirect to="/signIn"/>}
                <Grid item sm={12}>
                    <DateRange dateFrom={dateFrom}
                               dateTo={dateTo}
                               changeDateFrom={this.changeDateFrom}
                               changeDateTo={this.changeDateTo}
                    />
                </Grid>
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
