import React from 'react';
import {Container, Typography} from "@material-ui/core";
import {makeStyles} from "@material-ui/styles";
import {connect} from "react-redux";
import {Redirect} from "react-router-dom";
import {useTranslation} from "react-i18next";
import AdminClients from "./AdminClients";
import {isAdmin} from "../../store/selectors/authSelectors";

const useStyles = makeStyles(theme => ({
    root: {
        paddingTop: theme.spacing(3),
        paddingBottom: theme.spacing(4),
    },
    title: {
        marginBottom: theme.spacing(2),
    },
}));

const AdminDashboard = (props) => {

    const classes = useStyles();
    const {t} = useTranslation();

    // Belt-and-braces: App only mounts this route for admins, and the backend rejects the
    // request anyway, but a direct URL should not render an empty shell either.
    if (!props.userIsAdmin) {
        return <Redirect to="/"/>;
    }

    return (
        <Container maxWidth="lg" className={classes.root}>
            <Typography variant="h5" className={classes.title}>{t('admin.title')}</Typography>
            <AdminClients/>
        </Container>
    );
};

const mapStateToProps = state => ({
    userIsAdmin: isAdmin(state),
});

export default connect(mapStateToProps)(AdminDashboard);
