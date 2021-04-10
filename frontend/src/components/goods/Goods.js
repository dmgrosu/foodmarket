import React, {Component} from 'react';
import {Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Paper, TextField} from "@material-ui/core";
import {connect} from "react-redux";
import {withStyles} from "@material-ui/styles";
import {Redirect} from "react-router-dom";
import Filter from "./Filter";
import axios from "axios";
import Groups from "./Groups";
import Grid from "@material-ui/core/Grid";
import GoodsList from "./GoodsList";
import {handleError} from "../../store/actions/authActions";
import {addGoodToCart, changeQuantity, selectGood} from "../../store/actions/cartActions";

const styles = theme => ({
    root: {
        flexGrow: 1,
        padding: theme.spacing(2),
    },
    buttonProgress: {
        color: theme.palette.primary,
        position: 'absolute',
        left: '49%',
    },
});

class Goods extends Component {

    state = {
        filter: {
            brandId: 0,
            name: "",
            changed: false
        },
        allBrands: [],
        goods: [],
        groups: [],
        selectedGroupId: null,
        isFetchingGroups: false,
        isFetchingGoods: false,
    }

    changeFilter = (event, field) => {
        this.setState(state => ({
            filter: {
                ...state.filter,
                [field]: event.target.value,
                changed: true,
            },
        }))
    }

    setFetchingStarted = (goods, groups) => {
        this.setState(state => ({
            isFetchingGoods: goods,
            isFetchingGroups: groups,
            filter: {
                ...state.filter,
                changed: false,
            }
        }));
    }

    performSearch = () => {
        const {filter} = this.state;
        const brandId = filter.brandId === 0 ? null : filter.brandId;
        if (brandId === null && filter.name === '') {
            this.fetchGroups();
            return;
        }
        this.setFetchingStarted(true, true);
        axios.get("/good/search", {
            params: {brandId: brandId, name: filter.name},
            headers: {'Authorization': this.props.auth.token}
        }).then(resp => {
            const {data} = resp;
            this.setState({
                groups: data.groups,
                goods: [],
                isFetchingGoods: false,
                isFetchingGroups: false
            })
        }).catch(err => {
            this.handleFetchingError(err);
        })
    }

    handleFetchingError(err) {
        this.setState({
            isFetchingGroups: false,
            isFetchingGoods: false,
        });
        handleError(err);
    }

    fetchBrands = () => {
        axios.get("/brand/getAll", {headers: {'Authorization': this.props.auth.token}})
            .then(resp => {
                const {brands} = resp.data || [];
                this.setState({
                    allBrands: brands.filter(brand => brand.id && brand.name),
                })
            })
            .catch(err => {
                handleError(err);
            });
    }

    fetchGroups = () => {
        this.setFetchingStarted(false, true);
        axios.get("/good/listGroups", {headers: {'Authorization': this.props.auth.token}})
            .then(resp => {
                const {data} = resp;
                this.setState({
                    groups: data.groups,
                    goods: [],
                    isFetchingGroups: false,
                })
            })
            .catch(err => {
                this.handleFetchingError(err);
            })
    }

    fetchGoods = (event, groupId) => {
        const {filter} = this.state;
        let brandId = null;
        let nameLike = null;
        if (filter) {
            brandId = filter.brandId !== 0 ? filter.brandId : null;
            nameLike = filter.name !== '' ? filter.name : null;
        }
        this.setFetchingStarted(true, false);
        axios.get("/good/listGoods", {
            params: {
                groupId: groupId,
                brandId: brandId,
                name: nameLike
            },
            headers: {'Authorization': this.props.auth.token}
        })
            .then(resp => {
                const {data} = resp;
                this.setState({
                    goods: data.goods,
                    isFetchingGoods: false,
                })
            })
            .catch(err => {
                this.handleFetchingError(err);
            })
    }

    handleGoodSelect = (goodId) => {
        this.props.selectGood(goodId);
    }

    addToCart = () => {
        const {selectedGood, orderId} = this.props.cart;
        if (selectedGood) {
            this.props.addGoodToCart(selectedGood.id, orderId, selectedGood.quantity);
        }
    }

    changeSelectedGood = (event) => {
        this.props.changeQuantity(event.target.value);
    }

    componentDidMount() {
        this.fetchBrands();
        this.fetchGroups();
    }

    render() {

        const {auth, classes, cart} = this.props;
        const isAuthorized = auth.token !== null;
        const {filter, allBrands, goods, groups, isFetchingGroups, isFetchingGoods} = this.state;

        return (
            <Grid container className={classes.root}>
                {!isAuthorized && <Redirect to="/signIn"/>}
                <Grid item sm={12}>
                    <Filter brands={allBrands}
                            brandId={filter.brandId}
                            name={filter.name}
                            changeFilter={this.changeFilter}
                            search={this.performSearch}
                            changed={filter.changed}
                    />
                </Grid>
                <Grid container spacing={2} direction="row">
                    <Grid item xs={12} sm={3}>
                        <Paper elevation={3}>
                            <Groups groups={groups}
                                    handleSelect={this.fetchGoods}
                                    isFetching={isFetchingGroups}
                            />
                        </Paper>
                    </Grid>
                    <Grid item xs={12} sm={9}>
                        <Paper elevation={3}>
                            <GoodsList goods={goods}
                                       handleSelect={this.handleGoodSelect}
                                       isFetching={isFetchingGoods}
                            />
                        </Paper>
                    </Grid>
                </Grid>
                <Dialog open={cart.selectedGood.id !== null}
                        onClose={() => this.handleGoodSelect(null)}
                >
                    <DialogTitle>
                        Add to cart
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            Please input the desired quantity
                        </DialogContentText>
                        <TextField autoFocus
                                   fullWidth
                                   type="number"
                                   value={cart.selectedGood.quantity}
                                   onChange={this.changeSelectedGood}
                                   disabled={cart.isAdding}
                        />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={this.addToCart}
                                disabled={cart.isAdding}
                        >
                            OK
                        </Button>
                        {cart.isAdding && <CircularProgress size={28} className={classes.buttonProgress}/>}
                        <Button onClick={() => this.handleGoodSelect(null)}
                                disabled={cart.isAdding}
                        >
                            Cancel
                        </Button>
                    </DialogActions>
                </Dialog>
            </Grid>
        )
    }
}

const mapStateToProps = state => ({
    auth: state.authReducer,
    cart: state.cartReducer,
});

export default connect(mapStateToProps, {
    addGoodToCart,
    selectGood,
    changeQuantity
})(withStyles(styles)(Goods));
