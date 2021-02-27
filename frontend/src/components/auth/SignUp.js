import React, {Component} from 'react';
import Avatar from '@material-ui/core/Avatar';
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import TextField from '@material-ui/core/TextField';
import {Link, Redirect} from 'react-router-dom';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import SearchIcon from '@material-ui/icons/Search';
import Typography from '@material-ui/core/Typography';
import Container from '@material-ui/core/Container';
import Navbar from "../navigation/Navbar";
import Copyright from "../Copyright";
import {connect} from "react-redux";
import {withStyles} from "@material-ui/styles";
import {signUpStart} from "../../store/actions/authActions";
import {CircularProgress, IconButton} from "@material-ui/core";
import axios from "axios";
import {toast} from "material-react-toastify";


const styles = (theme) => ({
    paper: {
        marginTop: theme.spacing(8),
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
    },
    avatar: {
        margin: theme.spacing(1),
        backgroundColor: theme.palette.secondary.main,
    },
    buttonProgress: {
        color: theme.palette.primary,
        position: 'absolute',
        top: '50%',
        left: '50%',
        marginTop: -20,
        marginLeft: -20,
    },
    submit: {
        margin: theme.spacing(3, 0, 2),
    },
});

class SignUp extends Component {

    state = {
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: '',
        idno: '',
        entityFound: null,
        searching: false,
        errors: [],
    }

    signUp = () => {
        const {email, password, entityFound} = this.state;
        const clientId = entityFound && !entityFound.code ? entityFound.id : 0;
        if (this.validateInput()) {
            this.props.signUpStart(email, password, clientId);
        }
    }

    validateInput = () => {
        const {email, password, confirmPassword} = this.state;
        const errors = [];
        const emailRegEx = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        if (!email) {
            errors.push({
                field: 'email',
                code: 'EMAIL_EMPTY',
                description: 'Email required!'
            })
        } else if (!emailRegEx.test(email)) {
            errors.push({
                field: 'email',
                code: 'EMAIL_INVALID',
                description: 'Email is invalid!'
            })
        }
        const passRegexp = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,}$/;
        if (!password) {
            errors.push({
                field: 'password',
                code: 'PASSWORD_EMPTY',
                description: 'Password required!'
            })
        } else if (password !== confirmPassword) {
            errors.push({
                field: 'confirmPassword',
                code: 'PASSWORD_NOT_MATCH',
                description: 'Password does not match!'
            })
        } else if (!passRegexp.test(password)) {
            errors.push({
                field: 'password',
                code: 'PASSWORD_NOT_STRONG',
                description: 'Password is not strong!'
            })
        }
        if (errors.length > 0) {
            this.setState({
                errors: errors
            })
            return false;
        }
        return true;
    }

    findEntity = () => {
        this.setState({
            searching: true
        });
        const {idno} = this.state;
        axios.get("/client/findByIdno", {params: {idno: idno}})
            .then(resp => {
                if (resp.status === 200) {
                    this.setState({
                        entityFound: resp.data,
                        searching: false
                    })
                } else {
                    toast.warning(resp.status + ": " + resp.data.message);
                }
            })
            .catch(err => {
                this.setState({
                    searching: false,
                });
                toast.error(err.response.status + ": " + err.response.data.message || err.response.statusText);
            })
    }

    getClientInfo = () => {
        const {entityFound} = this.state;
        if (!entityFound) {
            return "";
        }
        if (entityFound.errors) {
            return entityFound.errors[0].code;
        } else {
            return "Found Client: " + entityFound.name + ", with IDNO: " + entityFound.idno;
        }
    }

    getErrorForField(fieldName) {
        const {errors} = this.state;
        if (errors.length === 0) {
            return false;
        }
        for (let i = 0; i < errors.length; i++) {
            const error = errors[i];
            if (error.field === fieldName) {
                return error.description;
            }
        }
        return false;
    }

    changeValue(field, newValue) {
        this.setState(state => ({
            [field]: newValue,
            errors: state.errors.filter(err => err.field !== field)
        }));
    }

    render = () => {

        const {classes, auth} = this.props;
        const {
            firstName, lastName, email, password,
            idno, searching, entityFound, confirmPassword
        } = this.state;

        return (
            <div>
                {auth.token && <Redirect to="/"/>}
                <Navbar/>
                <Container component="main" maxWidth="xs">
                    <CssBaseline/>
                    <div className={classes.paper}>
                        <Avatar className={classes.avatar}>
                            <LockOutlinedIcon/>
                        </Avatar>
                        <Typography component="h1" variant="h5" gutterBottom>
                            Sign up
                        </Typography>
                        <Grid container spacing={2}>
                            <Grid item xs={10}>
                                <TextField
                                    variant="outlined"
                                    fullWidth
                                    id="idno"
                                    label="IDNO"
                                    name="idno"
                                    value={idno}
                                    disabled={searching}
                                    onChange={(e) => this.setState({idno: e.target.value})}
                                />
                            </Grid>
                            <Grid item xs={2}>
                                <IconButton aria-label="search"
                                            disabled={!idno || searching || auth.isLoading}
                                            onClick={() => this.findEntity()}
                                >
                                    <SearchIcon fontSize="large"/>
                                    {searching && <CircularProgress size={36} className={classes.buttonProgress}/>}
                                </IconButton>
                            </Grid>
                            {entityFound && <Grid item xs={12}>
                                <Typography color={entityFound.errors ? "error" : "secondary"}>
                                    {this.getClientInfo()}
                                </Typography>
                            </Grid>}
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    name="firstName"
                                    variant="outlined"
                                    fullWidth
                                    id="firstName"
                                    label="First Name"
                                    autoFocus
                                    value={firstName}
                                    disabled={auth.isLoading}
                                    onChange={(e) => this.changeValue('firstName', e.target.value)}
                                />
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <TextField
                                    variant="outlined"
                                    fullWidth
                                    id="lastName"
                                    label="Last Name"
                                    name="lastName"
                                    value={lastName}
                                    disabled={auth.isLoading}
                                    onChange={(e) => this.changeValue('lastName', e.target.value)}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    variant="outlined"
                                    required
                                    fullWidth
                                    id="email"
                                    label="Email Address"
                                    name="email"
                                    error={this.getErrorForField("email") !== false}
                                    helperText={this.getErrorForField("email")}
                                    value={email}
                                    disabled={auth.isLoading}
                                    onChange={(e) => this.changeValue('email', e.target.value)}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    variant="outlined"
                                    required
                                    fullWidth
                                    name="password"
                                    label="Password"
                                    type="password"
                                    id="password"
                                    error={this.getErrorForField("password") !== false}
                                    helperText={this.getErrorForField("password")}
                                    value={password}
                                    disabled={auth.isLoading}
                                    onChange={(e) => this.changeValue('password', e.target.value)}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    variant="outlined"
                                    required
                                    fullWidth
                                    name="confirmPassword"
                                    label="Confirm password"
                                    error={this.getErrorForField("confirmPassword") !== false}
                                    helperText={this.getErrorForField("confirmPassword")}
                                    type="password"
                                    id="confirmPassword"
                                    value={confirmPassword}
                                    disabled={auth.isLoading}
                                    onChange={(e) => this.changeValue('confirmPassword', e.target.value)}
                                />
                            </Grid>
                        </Grid>
                        <Button
                            fullWidth
                            variant="contained"
                            color="primary"
                            onClick={() => this.signUp()}
                            className={classes.submit}
                            disabled={auth.isLoading}
                        >
                            Sign Up
                        </Button>
                        {auth.isLoading && <CircularProgress size={24} className={classes.buttonProgress}/>}
                        <Grid container justify="flex-end">
                            <Grid item>
                                <Link to="/signIn" variant="body2">
                                    Already have an account? Sign in
                                </Link>
                            </Grid>
                        </Grid>
                    </div>
                    <Box mt={5}>
                        <Copyright/>
                    </Box>
                </Container>
            </div>
        );
    }
}

const mapStateToProps = state => ({
    auth: state.authReducer
});

export default connect(mapStateToProps, {
    signUpStart
})(withStyles(styles)(SignUp));
