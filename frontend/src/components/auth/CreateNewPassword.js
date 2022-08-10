import Typography from "@material-ui/core/Typography";
import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";
import {CircularProgress} from "@material-ui/core";
import Container from "@material-ui/core/Container";
import React from "react";
import {connect} from "react-redux";
import {requestResetPasswordTokenValidation, resetPasswordStart, setNewPassword} from "../../store/actions/authActions";
import {withStyles} from "@material-ui/styles";
import {Redirect} from "react-router-dom";

const styles = (theme) => ({
    paper: {
        marginTop: theme.spacing(4),
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
    },
    buttonProgress: {
        color: theme.palette.primary,
        position: 'absolute',
        top: '50%',
        left: '50%',
        marginTop: -28,
        marginLeft: -12,
    },
});


class CreateNewPassword extends React.Component {
    state = {
        token: '',
        password: '',
        confirmPassword: '',
        errors: [],
        redirect: false
    }

    componentDidMount() {
        this.setState({
            token: this.props.match.params.token
        });
        this.requestTokenValidation(this.props.match.params.token);
    }

    requestTokenValidation = (resetToken) => {
        this.props.requestResetPasswordTokenValidation(resetToken);
        return true;
    }

    changeValue(field, e) {
        this.setState(state => ({
            [field]: e.target.value,
            errors: state.errors.filter(err => err.field !== field)
        }));
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

    changePassword = () => {
        if (this.validateInput()) {
            this.props.setNewPassword(this.state.token, this.state.password);
            this.setState({
                redirect: true
            });
        }
    }

    validateInput = () => {
        const {password, confirmPassword} = this.state;
        const errors = [];
        if (!password) {
            errors.push({
                field: 'password',
                code: 'PASSWORD_EMPTY',
                description: 'Password required!'
            })
        } else if (password !== confirmPassword) {
            errors.push({
                field: 'confirmPassword',
                code: 'PASSWORDS_DO_NOT_MATCH',
                description: 'Passwords must match!'
            })
        } else if (password.length < 8) {
            errors.push({
                field: 'password',
                code: 'PASSWORDS_TOO_SHORT',
                description: 'Passwords must be at least 8 characters long!'
            })
        }
        if (!confirmPassword) {
            errors.push({
                field: 'confirmPassword',
                code: 'CONFIRM_PASSWORD_EMPTY',
                description: 'Confirm password required!'
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

    render() {
        const {classes, auth} = this.props;
        const {isLoading, token} = auth;
        return (
            <Container component="main"
                       maxWidth="xs">
                {token && <Redirect to="/goods"/>}
                {this.state.redirect && <Redirect to="/signIn"/>}
                <div className={classes.paper}>
                    <Typography component="h1" variant="h5" gutterBottom>
                        New Password
                    </Typography>
                    <TextField
                        variant="outlined"
                        margin="normal"
                        required
                        fullWidth
                        id="password"
                        label="Password"
                        name="password"
                        autoComplete="password"
                        type="password"
                        autoFocus
                        error={this.getErrorForField("password") !== false}
                        helperText={this.getErrorForField("password")}
                        disabled={isLoading}
                        onChange={(e) => this.changeValue('password', e)}
                    />
                    <TextField
                        variant="outlined"
                        margin="normal"
                        required
                        fullWidth
                        id="confirmPassword"
                        label="Confirm password"
                        name="confirmPassword"
                        autoComplete="confirmPassword"
                        type="password"
                        error={this.getErrorForField("confirmPassword") !== false}
                        helperText={this.getErrorForField("confirmPassword")}
                        disabled={isLoading}
                        onChange={(e) => this.changeValue('confirmPassword', e)}
                    />
                    <Button
                        fullWidth
                        variant="contained"
                        color="primary"
                        disabled={isLoading}
                        className={classes.submit}
                        onClick={this.changePassword}
                    >
                        Reset
                    </Button>
                    {isLoading && <CircularProgress size={24} className={classes.buttonProgress}/>}
                </div>
            </Container>
        );
    }
}

const mapStateToProps = state => ({
    auth: state.authReducer
});

export default connect(mapStateToProps, ({
    requestResetPasswordTokenValidation,
    setNewPassword
}))(withStyles(styles)(CreateNewPassword));
