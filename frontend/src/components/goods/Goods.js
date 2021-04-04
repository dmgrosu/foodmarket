import React, {Component} from 'react';
import {Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Paper, TextField} from "@material-ui/core";
import {connect} from "react-redux";
import {withStyles} from "@material-ui/styles";
import {Redirect} from "react-router-dom";
import Filter from "./Filter";
import axios from "axios";
import Groups from "./Groups";
import Grid from "@material-ui/core/Grid";
import GoodsList from "./GoodsList";

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
        goods: [],
        groups: [],
        selectedGroupId: null,
        selectedGoodId: null,

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
        const {filter} = this.state;
        const brandId = filter.brandId === 0 ? null : filter.brandId;
        if (brandId === null && filter.name === '') {
            this.fetchGroups();
            return;
        }
        axios.get("/good/search", {
            params: {brandId: brandId, name: filter.name},
            headers: {'Authorization': this.props.auth.token}
        }).then(resp => {
            const {data} = resp;
            this.setState({
                groups: data.groups,
                goods: []
            })
        }).catch(err => {
            console.log(err);
        })
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
                console.log(err);
            });
    }

    fetchGroups = () => {
        axios.get("/good/listGroups", {headers: {'Authorization': this.props.auth.token}})
            .then(resp => {
                const {data} = resp;
                this.setState({
                    groups: data.groups,
                    goods: []
                })
            })
            .catch(err => {
                console.log(err);
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
                })
            })
            .catch(err => {
                console.log(err);
            })
    }

    handleGoodSelect = (goodId) => {
        this.setState({
            selectedGoodId: goodId
        })
    }

    addToCart = () => {

    }

    componentDidMount() {
        this.fetchBrands();
        this.fetchGroups();
    }

    render() {

        const {auth, classes} = this.props;
        const isAuthorized = auth.token !== null;
        const {filter, allBrands, goods, groups, selectedGoodId} = this.state;

        return (
            <Grid container className={classes.root}>
                {!isAuthorized && <Redirect to="/signIn"/>}
                <Grid item sm={12}>
                    <Filter brands={allBrands}
                            brandId={filter.brandId}
                            name={filter.name}
                            changeFilter={this.changeFilter}
                            search={this.performSearch}
                    />
                </Grid>
                <Grid container spacing={2} direction="row">
                    <Grid item xs={12} sm={3}>
                        <Paper elevation={3}>
                            <Groups groups={groups}
                                    handleSelect={this.fetchGoods}
                            />
                        </Paper>
                    </Grid>
                    <Grid item xs={12} sm={9}>
                        <Paper elevation={3}>
                            <GoodsList goods={goods}
                                       handleSelect={this.handleGoodSelect}
                            />
                        </Paper>
                    </Grid>
                </Grid>
                <Dialog open={selectedGoodId !== null}
                        onClose={() => this.setState({selectedGoodId: null})}
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
                        />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={this.addToCart}>
                            OK
                        </Button>
                        <Button onClick={() => this.setState({selectedGoodId: null})}>
                            Cancel
                        </Button>
                    </DialogActions>
                </Dialog>
            </Grid>
        )
    }
}

const mapStateToProps = state => ({
    auth: state.authReducer
});

export default connect(mapStateToProps, {})(withStyles(styles)(Goods));
