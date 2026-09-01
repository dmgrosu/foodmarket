import React, {Component} from 'react';
import Avatar from '@material-ui/core/Avatar';
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import {Link, Redirect} from 'react-router-dom';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import Typography from '@material-ui/core/Typography';
import Copyright from "../home/Copyright";
import {withStyles} from "@material-ui/styles";
import {connect} from "react-redux";
import {loginStart} from "../../store/actions/authActions";
import {CircularProgress, Container} from "@material-ui/core";
import {withTranslation} from "react-i18next";
import {VALIDATION_MESSAGE_KEYS} from "../../i18n/validationCodes";


const styles = (theme) => ({
    paper: {
        marginTop: theme.spacing(4),
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
            })
        }
        if (!password) {
            errors.push({
                field: 'password',
                code: 'PASSWORD_EMPTY',
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

    // Errors are stored as {field, code} and translated here, at render time, so a language
    // switch after a failed submit updates the message instead of leaving it frozen in
    // whatever language was active when validateInput() ran.
    getErrorForField(fieldName) {
        const {errors} = this.state;
        const {t} = this.props;
        if (errors.length === 0) {
            return false;
        }
        for (let i = 0; i < errors.length; i++) {
            const error = errors[i];
            if (error.field === fieldName) {
                return t(VALIDATION_MESSAGE_KEYS[error.code]);
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

        const {classes, auth, t} = this.props;
        const {isLoading, token} = auth;

        return (
            <Container component="main"
                       maxWidth="xs"
            >
                {token && <Redirect to="/products"/>}
                <div className={classes.paper}>
                    <Avatar className={classes.avatar}>
                        <LockOutlinedIcon/>
                    </Avatar>
                    <Typography component="h1" variant="h5">
                        {t('auth.signIn.title')}
                    </Typography>
                    <TextField
                        variant="outlined"
                        margin="normal"
                        required
                        fullWidth
                        id="email"
                        label={t('auth.fields.email')}
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
                        label={t('auth.fields.password')}
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
                        {t('auth.signIn.submit')}
                    </Button>
                    {isLoading && <CircularProgress size={24} className={classes.buttonProgress}/>}
                    <Grid container>
                        <Grid item xs={6}>
                            <Link to="/forgotPassword">
                                {t('auth.signIn.forgotPassword')}
                            </Link>
                        </Grid>
                        <Grid item xs={6}>
                            <Link to="/signUp">
                                {t('auth.signIn.noAccount')}
                            </Link>
                        </Grid>
                    </Grid>
                </div>
                <Box mt={8}>
                    <Copyright/>
                </Box>
            </Container>
        );
    }

}

const mapStateToProps = state => ({
    auth: state.authReducer,
});

export default withTranslation()(connect(mapStateToProps, {
    loginStart
})(withStyles(styles)(SignIn)));
