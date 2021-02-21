import React, {Component} from 'react';
import Avatar from '@material-ui/core/Avatar';
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import TextField from '@material-ui/core/TextField';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import {Link} from 'react-router-dom';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import Typography from '@material-ui/core/Typography';
import Container from '@material-ui/core/Container';
import Navbar from "../Navbar";
import Copyright from "../Copyright";
import {withStyles} from "@material-ui/styles";
import {connect} from "react-redux";
import {loginStart} from "../../store/actions/authActions";


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
    submit: {
        margin: theme.spacing(3, 0, 2),
    },
});

class SignIn extends Component {

    state = {
        email: '',
        password: ''
    }

    changeEmail = (newEmail) => {
        this.setState({
            email: newEmail
        })
    }

    changePassword = (newPasswd) => {
        this.setState({
            password: newPasswd
        })
    }

    render() {

        const {classes, auth} = this.props;
        const {isLoading} = auth;

        return (
            <div>
                <Navbar/>
                <Container component="main" maxWidth="xs">
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
                            disabled={isLoading}
                            onChange={(e) => this.changeEmail(e.target.value)}
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
                            autoComplete="current-password"
                            onChange={(e) => this.changePassword(e.target.value)}
                        />
                        <FormControlLabel
                            control={<Checkbox value="remember" color="primary"/>}
                            disabled={isLoading}
                            label="Remember me"
                        />
                        <Button
                            fullWidth
                            variant="contained"
                            color="primary"
                            disabled={isLoading}
                            className={classes.submit}
                            onClick={() => this.requestLogin()}
                        >
                            Sign In
                        </Button>
                        <Grid container>
                            <Grid item xs>
                                <Link to='/' variant="body2">
                                    Forgot password?
                                </Link>
                            </Grid>
                            <Grid item>
                                <Link to='/signUp' variant="body2">
                                    {"Don't have an account? Sign Up"}
                                </Link>
                            </Grid>
                        </Grid>
                    </div>
                    <Box mt={8}>
                        <Copyright/>
                    </Box>
                </Container>
            </div>
        );
    }

    requestLogin = () => {
        const {email, password} = this.state;
        this.props.loginStart(email, password);
    }
}

const mapStateToProps = state => ({
    auth: state.authReducer
});

export default connect(mapStateToProps, {
    loginStart
})(withStyles(styles)(SignIn));
