import React, {useCallback, useEffect, useRef, useState} from 'react';
import {
    Chip,
    CircularProgress,
    Collapse,
    Grid,
    IconButton,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    TextField,
    Typography
} from "@material-ui/core";
import {KeyboardArrowDown, KeyboardArrowUp} from "@material-ui/icons";
import {makeStyles} from "@material-ui/styles";
import {useTranslation} from "react-i18next";
import axios from "../../axios-instance";
import {handleError} from "../../store/actions/authActions";

const useStyles = makeStyles(theme => ({
    root: {
        padding: theme.spacing(2),
        maxWidth: 1400,
        margin: "auto",
    },
    toolbar: {
        display: "flex",
        alignItems: "flex-start",
        gap: theme.spacing(2),
        flexWrap: "wrap",
        padding: theme.spacing(2),
    },
    stateCell: {
        padding: theme.spacing(4),
        textAlign: "center",
    },
    head: {
        backgroundColor: '#bdbdbd',
    },
    itemsCell: {
        paddingTop: 0,
        paddingBottom: 0,
    },
    items: {
        margin: theme.spacing(1, 0, 2, 0),
    },
}));

const ROWS_PER_PAGE_OPTIONS = [10, 25, 50];
const DAY_MS = 24 * 60 * 60 * 1000;
const DEFAULT_PERIOD_DAYS = 30;

/**
 * The colour each order state is shown in. EXPORTED means the order has been written into a file
 * for the ERP; PROCESSED and NOT_PROCESSED are reserved and nothing sets them today.
 */
const STATE_COLOURS = {
    NEW: "default",
    PLACED: "primary",
    EXPORTED: "primary",
    PROCESSED: "primary",
    NOT_PROCESSED: "secondary",
};

const toDateInput = (millis) => new Date(millis).toISOString().slice(0, 10);

const OrderItems = ({order, classes, t}) => (
    <div className={classes.items}>
        <Table size="small">
            <TableHead>
                <TableRow>
                    <TableCell>{t('cart.columns.name')}</TableCell>
                    <TableCell align="center">{t('cart.columns.price')}</TableCell>
                    <TableCell align="center">{t('cart.columns.quantity')}</TableCell>
                    <TableCell align="right">{t('cart.columns.sum')}</TableCell>
                </TableRow>
            </TableHead>
            <TableBody>
                {order.items.map(item => (
                    <TableRow key={item.productId}>
                        <TableCell>{item.productName}</TableCell>
                        <TableCell align="center">{item.price.toFixed(2)}</TableCell>
                        <TableCell align="center">{item.quantity}</TableCell>
                        <TableCell align="right">{item.sum.toFixed(2)}</TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    </div>
);

const OrderRow = ({order, classes, t}) => {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <>
            <TableRow hover>
                <TableCell style={{width: 20}}>
                    <IconButton size="small" onClick={() => setIsOpen(!isOpen)}>
                        {isOpen ? <KeyboardArrowUp/> : <KeyboardArrowDown/>}
                    </IconButton>
                </TableCell>
                <TableCell>{order.id}</TableCell>
                <TableCell>{new Date(order.createdAt).toLocaleString()}</TableCell>
                <TableCell align="center">
                    <Chip size="small"
                          label={t(`orders.states.${order.state}`)}
                          color={STATE_COLOURS[order.state] || "default"}
                    />
                </TableCell>
                <TableCell align="center">{order.items.length}</TableCell>
                <TableCell align="right">{order.totalWeight.toFixed(2)}</TableCell>
                <TableCell align="right">{order.totalSum.toFixed(2)}</TableCell>
            </TableRow>
            <TableRow>
                <TableCell className={classes.itemsCell} colSpan={7}>
                    <Collapse in={isOpen} timeout="auto" unmountOnExit>
                        <OrderItems order={order} classes={classes} t={t}/>
                    </Collapse>
                </TableCell>
            </TableRow>
        </>
    );
};

/**
 * The client's own order history over a date range.
 *
 * Orders are read for the authenticated user's client — the endpoint takes no client id, so this
 * page cannot ask for anybody else's.
 */
const Orders = () => {

    const classes = useStyles();
    const {t} = useTranslation();

    const [dateFrom, setDateFrom] = useState(toDateInput(Date.now() - DEFAULT_PERIOD_DAYS * DAY_MS));
    const [dateTo, setDateTo] = useState(toDateInput(Date.now()));
    const [orders, setOrders] = useState([]);
    const [totalElements, setTotalElements] = useState(0);
    const [pageNo, setPageNo] = useState(0);
    const [pageSize, setPageSize] = useState(ROWS_PER_PAGE_OPTIONS[1]);
    const [isLoading, setIsLoading] = useState(false);

    // Changing a date and a page a moment apart puts two requests in flight, and they can resolve
    // out of order. Each takes a sequence number and only the newest may touch state.
    const latestRequestRef = useRef(0);

    const loadPage = useCallback(() => {
        const requestId = latestRequestRef.current + 1;
        latestRequestRef.current = requestId;
        const isStale = () => requestId !== latestRequestRef.current;

        setIsLoading(true);
        axios.post("/order/getOrdersByPeriod", {
            // The end date is inclusive, so the range runs to the end of that day.
            dateFrom: new Date(dateFrom).getTime(),
            dateTo: new Date(dateTo).getTime() + DAY_MS - 1,
            pageNo: pageNo,
            pageSize: pageSize,
            sortColumn: "createdAt",
            sortDirection: "DESC",
        })
            .then(resp => {
                if (isStale()) {
                    return;
                }
                setOrders(resp.data.orders || []);
                setTotalElements(resp.data.totalElements || 0);
                setIsLoading(false);
            })
            .catch(err => {
                if (isStale()) {
                    return;
                }
                setOrders([]);
                setTotalElements(0);
                setIsLoading(false);
                handleError(err);
            });
    }, [dateFrom, dateTo, pageNo, pageSize]);

    useEffect(() => {
        loadPage();
    }, [loadPage]);

    const changeDateFrom = (value) => {
        setPageNo(0);
        setDateFrom(value);
    };

    const changeDateTo = (value) => {
        setPageNo(0);
        setDateTo(value);
    };

    return (
        <Grid container className={classes.root}>
            <Grid item sm={12}>
                <Typography variant="h5">{t('orders.title')}</Typography>
            </Grid>
            <Grid item sm={12}>
                <Paper elevation={3}>
                    <div className={classes.toolbar}>
                        <TextField label={t('orders.dateFrom')}
                                   type="date"
                                   value={dateFrom}
                                   InputLabelProps={{shrink: true}}
                                   onChange={event => changeDateFrom(event.target.value)}
                        />
                        <TextField label={t('orders.dateTo')}
                                   type="date"
                                   value={dateTo}
                                   InputLabelProps={{shrink: true}}
                                   onChange={event => changeDateTo(event.target.value)}
                        />
                    </div>
                    <TableContainer>
                        <Table size="small">
                            <TableHead>
                                <TableRow>
                                    <TableCell className={classes.head}/>
                                    <TableCell className={classes.head}>{t('orders.columns.number')}</TableCell>
                                    <TableCell className={classes.head}>{t('orders.columns.date')}</TableCell>
                                    <TableCell className={classes.head} align="center">
                                        {t('orders.columns.state')}
                                    </TableCell>
                                    <TableCell className={classes.head} align="center">
                                        {t('orders.columns.positions')}
                                    </TableCell>
                                    <TableCell className={classes.head} align="right">
                                        {t('orders.columns.weight')}
                                    </TableCell>
                                    <TableCell className={classes.head} align="right">
                                        {t('orders.columns.total')}
                                    </TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {isLoading && orders.length === 0 &&
                                <TableRow>
                                    <TableCell colSpan={7} className={classes.stateCell}>
                                        <CircularProgress size={28}/>
                                    </TableCell>
                                </TableRow>}
                                {!isLoading && orders.length === 0 &&
                                <TableRow>
                                    <TableCell colSpan={7} className={classes.stateCell}>
                                        <Typography color="textSecondary">{t('orders.empty')}</Typography>
                                    </TableCell>
                                </TableRow>}
                                {orders.map(order => (
                                    <OrderRow key={order.id} order={order} classes={classes} t={t}/>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <TablePagination component="div"
                                     rowsPerPageOptions={ROWS_PER_PAGE_OPTIONS}
                                     count={totalElements}
                                     page={pageNo}
                                     rowsPerPage={pageSize}
                                     labelRowsPerPage={t('products.table.rowsPerPage')}
                                     onChangePage={(event, page) => setPageNo(page)}
                                     onChangeRowsPerPage={event => {
                                         setPageSize(parseInt(event.target.value, 10));
                                         setPageNo(0);
                                     }}
                    />
                </Paper>
            </Grid>
        </Grid>
    );
};

export default Orders;
