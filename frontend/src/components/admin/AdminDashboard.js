import React from 'react';
import {Container, Paper, Tab, Tabs, Typography} from "@material-ui/core";
import {makeStyles} from "@material-ui/styles";
import {connect} from "react-redux";
import {Link, Redirect, Route, Switch, useLocation} from "react-router-dom";
import AdminBrands from "./AdminBrands";
import AdminClients from "./AdminClients";
import AdminProducts from "./AdminProducts";
import {isAdmin} from "../../store/selectors/authSelectors";

const useStyles = makeStyles(theme => ({
    root: {
        paddingTop: theme.spacing(3),
        paddingBottom: theme.spacing(4),
    },
    title: {
        marginBottom: theme.spacing(2),
    },
    tabs: {
        marginBottom: theme.spacing(2),
    },
}));

const TABS = [
    {path: "/admin/products", label: "Товары"},
    {path: "/admin/brands", label: "Бренды"},
    {path: "/admin/clients", label: "Клиенты"},
];

const AdminDashboard = (props) => {

    const classes = useStyles();
    const location = useLocation();

    const activeTab = TABS.find(tab => location.pathname.startsWith(tab.path));
    const activeTabPath = activeTab ? activeTab.path : TABS[0].path;

    // Belt-and-braces: App only mounts this route for admins, and the backend rejects the
    // requests anyway, but a direct URL should not render an empty shell either.
    if (!props.userIsAdmin) {
        return <Redirect to="/"/>;
    }

    return (
        <Container maxWidth="lg" className={classes.root}>
            <Typography variant="h5" className={classes.title}>Панель администратора</Typography>
            <Paper className={classes.tabs}>
                <Tabs value={activeTabPath}
                      indicatorColor="primary"
                      textColor="primary"
                >
                    {TABS.map(tab => (
                        <Tab key={tab.path} value={tab.path} label={tab.label} component={Link} to={tab.path}/>
                    ))}
                </Tabs>
            </Paper>
            <Switch>
                <Route path="/admin/products" component={AdminProducts}/>
                <Route path="/admin/brands" component={AdminBrands}/>
                <Route path="/admin/clients" component={AdminClients}/>
                <Redirect to="/admin/products"/>
            </Switch>
        </Container>
    );
};

const mapStateToProps = state => ({
    userIsAdmin: isAdmin(state),
});

export default connect(mapStateToProps)(AdminDashboard);
