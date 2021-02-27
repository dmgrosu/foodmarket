import React, {Component} from 'react';
import Avatar from '@material-ui/core/Avatar';
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import TextField from '@material-ui/core/TextField';
import {Link, Redirect} from 'react-router-dom';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import Typography from '@material-ui/core/Typography';
import Copyright from "../Copyright";
import {withStyles} from "@material-ui/styles";
import {connect} from "react-redux";
import {loginStart} from "../../store/actions/authActions";
import {CircularProgress} from "@material-ui/core";


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
        marginTop: -28,
        marginLeft: -12,
    },
    submit: {
        margin: theme.spacing(3, 0, 2),
        position: 'relative',
    },
});

class SignIn extends Component {

    state = {
        email: '',
        password: '',
        errors: []
    }

    validateInput = () => {
        const {email, password} = this.state;
        const errors = [];
        if (!email) {
            errors.push({
                field: 'email',
                code: 'EMAIL_EMPTY',
                description: 'Email required!'
            })
        }
        if (!password) {
            errors.push({
                field: 'password',
                code: 'PASSWORD_EMPTY',
                description: 'Password required!'
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

    changeValue(field, e) {
        this.setState(state => ({
            [field]: e.target.value,
            errors: state.errors.filter(err => err.field !== field)
        }));
    }

    requestLogin = () => {
        const {email, password} = this.state;
        if (this.validateInput()) {
            this.props.loginStart(email, password);
        }
    }

    render() {

        const {classes, auth} = this.props;
        const {isLoading, token} = auth;

        return (
            <div>
                {token && <Redirect to='/'/>}
                <CssBaseline/>
                <div className={classes.paper}>
                    <Avatar className={classes.avatar}>
                        <LockOutlinedIcon/>
                    </Avatar>
                    <Typography component="h1" variant="h5">
                        Sign in
                    </Typography>
                    <TextField
                        variant="outlined"
                        margin="normal"
                        required
                        fullWidth
                        id="email"
                        label="Email Address"
                        name="email"
                        autoComplete="email"
                        autoFocus
                        error={this.getErrorForField("email") !== false}
                        helperText={this.getErrorForField("email")}
                        disabled={isLoading}
                        onChange={(e) => this.changeValue('email', e)}
                    />
                    <TextField
                        variant="outlined"
                        margin="normal"
                        required
                        fullWidth
                        name="password"
                        label="Password"
                        type="password"
                        id="password"
                        disabled={isLoading}
                        error={this.getErrorForField("password") !== false}
                        helperText={this.getErrorForField("password")}
                        autoComplete="current-password"
                        onChange={(e) => this.changeValue('password', e)}
                    />
                    <Button
                        fullWidth
                        variant="contained"
                        color="primary"
                        disabled={isLoading}
                        className={classes.submit}
                        onClick={this.requestLogin}
                    >
                        Sign In
                    </Button>
                    {isLoading && <CircularProgress size={24} className={classes.buttonProgress}/>}
                    <Grid container>
                        <Grid item xs={6}>
                            {/*<Link to="/" >*/}
                            {/*    Forgot password?*/}
                            {/*</Link>*/}
                        </Grid>
                        <Grid item xs={6}>
                            <Link to="/signUp">
                                {"Don't have an account? Sign Up"}
                            </Link>
                        </Grid>
                    </Grid>
                </div>
                <Box mt={8}>
                    <Copyright/>
                </Box>
            </div>
        );
    }

}

const mapStateToProps = state => ({
    auth: state.authReducer,
});

export default connect(mapStateToProps, {
    loginStart
})(withStyles(styles)(SignIn));
