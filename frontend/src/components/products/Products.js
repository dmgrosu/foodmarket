import React, {Component} from 'react';
import {
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Paper,
    TextField
} from "@material-ui/core";
import {connect} from "react-redux";
import {withStyles} from "@material-ui/styles";
import {Redirect} from "react-router-dom";
import Filter from "./Filter";
import axios from "../../axios-instance";
import Groups from "./Groups";
import Grid from "@material-ui/core/Grid";
import ProductsList from "./ProductsList";
import {handleError} from "../../store/actions/authActions";
import {addProductToCart, changeQuantity, fetchCart, selectProduct} from "../../store/actions/cartActions";
import {withTranslation} from "react-i18next";

const ROWS_PER_PAGE_OPTIONS = [10, 25, 50];

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

class Products extends Component {

    state = {
        filter: {
            storageId: 0,
            brandId: 0,
            name: "",
            changed: false
        },
        allStorages: [],
        allBrands: [],
        products: [],
        groups: [],
        childrenByGroupId: {},
        groupHasChildrenById: {},
        expandedGroupIds: [],
        loadingGroupIds: [],
        selectedGroupId: null,
        pageNo: 0,
        pageSize: ROWS_PER_PAGE_OPTIONS[1],
        totalProducts: 0,
        isFetchingGroups: false,
        isFetchingProducts: false,
    }

    // A group click and a page click a moment apart put two requests in flight, and they can
    // resolve out of order. Each takes a sequence number and only the newest may touch state.
    latestProductRequest = 0;

    changeFilter = (event, field) => {
        this.setState(state => ({
            filter: {
                ...state.filter,
                [field]: event.target.value,
                changed: true,
            },
        }))
    }

    setFetchingStarted = (products, groups) => {
        this.setState(state => ({
            isFetchingProducts: products,
            isFetchingGroups: groups,
            filter: {
                ...state.filter,
                changed: false,
            }
        }));
    }

    performSearch = () => {
        // Filters apply to the whole catalogue, so whichever group was open no longer bounds the
        // result. The tree stays put as the browse affordance.
        this.setState({selectedGroupId: null, pageNo: 0}, this.loadProducts);
    }

    handleFetchingError(err) {
        this.setState({
            isFetchingGroups: false,
            isFetchingProducts: false,
        });
        handleError(err);
    }

    fetchBrands = () => {
        axios.get("/brand/getAll")
            .then(resp => {
                const brands = resp.data || [];
                this.setState({
                    allBrands: brands.filter(brand => brand.id && brand.name),
                })
            })
            .catch(err => {
                handleError(err);
            });
    }

    fetchStorages = () => {
        axios.get("/storage")
            .then(resp => {
                const storages = resp.data || [];
                this.setState({
                    allStorages: storages.filter(storage => storage.id && storage.name),
                })
            })
            .catch(err => {
                handleError(err);
            });
    }

    fetchGroups = () => {
        this.setFetchingStarted(false, true);
        axios.get("/product/listGroups")
            .then(resp => {
                const roots = resp.data || [];
                this.setState(state => ({
                    groups: roots,
                    groupHasChildrenById: this.withHasChildren(state.groupHasChildrenById, roots),
                    isFetchingGroups: false,
                }))
            })
            .catch(err => {
                this.handleFetchingError(err);
            })
    }

    withHasChildren = (known, groups) => {
        const updated = {...known};
        groups.forEach(group => {
            updated[group.id.toString()] = group.hasChildren;
        });
        return updated;
    }

    toggleGroups = (event, nodeIds) => {
        this.setState({expandedGroupIds: nodeIds});
        nodeIds
            .filter(nodeId => this.state.childrenByGroupId[nodeId] === undefined
                && !this.state.loadingGroupIds.includes(nodeId))
            .forEach(this.loadChildGroups);
    }

    loadChildGroups = (parentGroupId) => {
        this.setState(state => ({loadingGroupIds: [...state.loadingGroupIds, parentGroupId]}));
        axios.get("/product/listGroups", {params: {parentGroupId}})
            .then(resp => {
                const children = resp.data || [];
                this.setState(state => ({
                    childrenByGroupId: {...state.childrenByGroupId, [parentGroupId]: children},
                    groupHasChildrenById: this.withHasChildren(state.groupHasChildrenById, children),
                    loadingGroupIds: state.loadingGroupIds.filter(id => id !== parentGroupId),
                }))
            })
            .catch(err => {
                this.setState(state => ({
                    loadingGroupIds: state.loadingGroupIds.filter(id => id !== parentGroupId),
                }));
                handleError(err);
            })
    }

    selectGroup = (event, groupId) => {
        // A folder holds no products of its own, so clicking one opens it rather than running a
        // search that could only come back empty.
        if (this.state.groupHasChildrenById[groupId]) {
            const expanded = this.state.expandedGroupIds.includes(groupId)
                ? this.state.expandedGroupIds.filter(id => id !== groupId)
                : [...this.state.expandedGroupIds, groupId];
            this.toggleGroups(event, expanded);
            return;
        }
        // A different group is a different result set, so the page number cannot carry over.
        this.setState({selectedGroupId: groupId, pageNo: 0}, this.loadProducts);
    }

    changePage = (pageNo) => {
        this.setState({pageNo}, this.loadProducts);
    }

    changePageSize = (pageSize) => {
        this.setState({pageSize, pageNo: 0}, this.loadProducts);
    }

    loadProducts = () => {
        const {filter, selectedGroupId, pageNo, pageSize} = this.state;
        const requestId = ++this.latestProductRequest;
        this.setFetchingStarted(true, false);
        axios.get("/product/search", {
            params: {
                storageId: filter.storageId !== 0 ? filter.storageId : null,
                groupId: selectedGroupId,
                brandId: filter.brandId !== 0 ? filter.brandId : null,
                name: filter.name !== '' ? filter.name : null,
                pageNo: pageNo,
                pageSize: pageSize,
            }
        })
            .then(resp => {
                if (requestId !== this.latestProductRequest) {
                    return;
                }
                const {data} = resp;
                this.setState({
                    products: data.items || [],
                    totalProducts: data.totalElements || 0,
                    isFetchingProducts: false,
                })
            })
            .catch(err => {
                if (requestId !== this.latestProductRequest) {
                    return;
                }
                this.handleFetchingError(err);
            })
    }

    handleProductSelect = (productId) => {
        this.props.selectProduct(productId);
    }

    addToCart = () => {
        const {selectedProduct} = this.props.cart;
        if (selectedProduct && !this.blockedFromAdding()) {
            this.props.addProductToCart(selectedProduct.id, this.state.filter.storageId,
                Number(selectedProduct.quantity));
        }
    }

    /**
     * Why the selected product cannot be added right now, or null when it can.
     *
     * A line is priced per (product, storage, tier), so an order is tied to one storage: the
     * catalogue's "all storages" view has no price to order at, and a cart already opened against
     * another storage cannot take this one.
     */
    blockedFromAdding = () => {
        const {filter} = this.state;
        const {cart, t} = this.props;

        if (filter.storageId === 0) {
            return t('products.addToCart.selectStorage');
        }
        if (cart.storageId !== null && cart.storageId !== undefined && cart.storageId !== filter.storageId) {
            return t('products.addToCart.storageLocked');
        }
        if (!(Number(cart.selectedProduct.quantity) >= 1)) {
            return t('products.addToCart.quantityTooSmall');
        }
        return null;
    }

    changeSelectedProduct = (event) => {
        this.props.changeQuantity(event.target.value);
    }

    componentDidMount() {
        this.fetchStorages();
        this.fetchBrands();
        this.fetchGroups();
        // The cart lives on the server; this is what recovers it after a reload.
        this.props.fetchCart();
    }

    render() {

        const {auth, classes, cart, t} = this.props;
        const isAuthorized = auth.token !== null;
        const {
            filter, allStorages, allBrands, products, groups, childrenByGroupId, expandedGroupIds,
            loadingGroupIds, selectedGroupId, pageNo, pageSize, totalProducts,
            isFetchingGroups, isFetchingProducts
        } = this.state;
        const addBlockedReason = cart.selectedProduct.id !== null ? this.blockedFromAdding() : null;

        return (
            <Grid container className={classes.root}>
                {!isAuthorized && <Redirect to="/signIn"/>}
                <Grid item sm={12}>
                    <Filter storages={allStorages}
                            storageId={filter.storageId}
                            brands={allBrands}
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
                                    childrenByGroupId={childrenByGroupId}
                                    loadingGroupIds={loadingGroupIds}
                                    expanded={expandedGroupIds}
                                    selected={selectedGroupId === null ? '' : selectedGroupId}
                                    handleToggle={this.toggleGroups}
                                    handleSelect={this.selectGroup}
                                    isFetching={isFetchingGroups}
                            />
                        </Paper>
                    </Grid>
                    <Grid item xs={12} sm={9}>
                        <Paper elevation={3}>
                            <ProductsList products={products}
                                          handleSelect={this.handleProductSelect}
                                          isFetching={isFetchingProducts}
                                          pageNo={pageNo}
                                          pageSize={pageSize}
                                          totalProducts={totalProducts}
                                          rowsPerPageOptions={ROWS_PER_PAGE_OPTIONS}
                                          changePage={this.changePage}
                                          changePageSize={this.changePageSize}
                            />
                        </Paper>
                    </Grid>
                </Grid>
                <Dialog open={cart.selectedProduct.id !== null}
                        onClose={() => this.handleProductSelect(null)}
                >
                    <DialogTitle>
                        {t('products.addToCart.title')}
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            {t('products.addToCart.prompt')}
                        </DialogContentText>
                        <TextField autoFocus
                                   fullWidth
                                   type="number"
                                   inputProps={{min: 1}}
                                   value={cart.selectedProduct.quantity}
                                   onChange={this.changeSelectedProduct}
                                   disabled={cart.isAdding}
                        />
                        {addBlockedReason &&
                        <DialogContentText color="error">
                            {addBlockedReason}
                        </DialogContentText>}
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={this.addToCart}
                                disabled={cart.isAdding || addBlockedReason !== null}
                        >
                            {t('common.ok')}
                        </Button>
                        {cart.isAdding && <CircularProgress size={28} className={classes.buttonProgress}/>}
                        <Button onClick={() => this.handleProductSelect(null)}
                                disabled={cart.isAdding}
                        >
                            {t('common.cancel')}
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

export default withTranslation()(connect(mapStateToProps, {
    addProductToCart,
    fetchCart,
    selectProduct,
    changeQuantity
})(withStyles(styles)(Products)));
