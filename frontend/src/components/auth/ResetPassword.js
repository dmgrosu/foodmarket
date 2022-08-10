import React from "react";
import {connect} from "react-redux";
import {requestResetPasswordTokenValidation, resetPasswordStart, signUpStart} from "../../store/actions/authActions";
import {withStyles} from "@material-ui/styles";
import Container from "@material-ui/core/Container";
import Typography from "@material-ui/core/Typography";
import TextField from "@material-ui/core/TextField";
import {Redirect} from "react-router-dom";
import Button from "@material-ui/core/Button";
import {CircularProgress} from "@material-ui/core";
import {toast} from "material-react-toastify";

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

class ResetPassword extends React.Component {
    state = {
        email: '',
        errors: []
    }

    changeValue(field, e) {
        this.setState(state => ({
            [field]: e.target.value,
            errors: state.errors.filter(err => err.field !== field)
        }));
    }

    validateInput = () => {
        const {email} = this.state;
        const errors = [];
        if (!email) {
            errors.push({
                field: 'email',
                code: 'EMAIL_EMPTY',
                description: 'Email required!'
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

    requestResetPassword = () => {
        const {email} = this.state;
        if (this.validateInput()) {
            this.props.resetPasswordStart(email);
            toast.success("Email sent!");
            this.setState({
                email: ''
            });
        }
    }

    render() {
        const {classes, auth} = this.props;
        const {isLoading, token} = auth;
        return (
            <Container component="main"
                       maxWidth="xs">
                {token && <Redirect to="/goods"/>}
                <div className={classes.paper}>
                    <Typography component="h1" variant="h5" gutterBottom>
                        Reset Password
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
                        value={this.state.email}
                        error={this.getErrorForField("email") !== false}
                        helperText={this.getErrorForField("email")}
                        disabled={isLoading}
                        onChange={(e) => this.changeValue('email', e)}
                    />
                    <Button
                        fullWidth
                        variant="contained"
                        color="primary"
                        disabled={isLoading}
                        className={classes.submit}
                        onClick={this.requestResetPassword}
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
    resetPasswordStart,
}))(withStyles(styles)(ResetPassword));
