import React, {Component} from 'react';
import Avatar from '@material-ui/core/Avatar';
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import TextField from '@material-ui/core/TextField';
import {Link} from 'react-router-dom';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import SearchIcon from '@material-ui/icons/Search';
import Typography from '@material-ui/core/Typography';
import Container from '@material-ui/core/Container';
import Navbar from "../Navbar";
import Copyright from "../Copyright";
import {connect} from "react-redux";
import {withStyles} from "@material-ui/styles";
import {signUpStart} from "../../store/actions/authActions";
import {IconButton, MenuItem, Select} from "@material-ui/core";
import axios from "axios";

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
    form: {
        width: '100%', // Fix IE 11 issue.
        marginTop: theme.spacing(3),
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
        orgForm: 0,
        idno: '',
        entityFound: null,
        searching: false
    }

    signUp = () => {
        const {firstName, lastName, email, password} = this.state;
        this.props.signUpStart(firstName, lastName, email, password);
    }

    findEntity = () => {
        this.setState({
            searching: true
        });
        const {idno} = this.state;
        axios.get("/client/findByIdno", {params: {idno: idno}})
            .then(resp => {
                this.setState({
                    entityFound: resp.data,
                    searching: false
                })
            })
    }

    render = () => {

        const {classes} = this.props;
        const {firstName, lastName, email, password, orgForm, idno, searching, entityFound} = this.state;

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
                            Sign up
                        </Typography>
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <Select value={orgForm}
                                        fullWidth
                                        variant="outlined"
                                        onChange={(e) => this.setState({orgForm: e.target.value})}
                                >
                                    <MenuItem value={0}>Person</MenuItem>
                                    <MenuItem value={1}>Entity</MenuItem>
                                </Select>
                            </Grid>
                            {orgForm === 0 && <Grid item xs={12} sm={6}>
                                <TextField
                                    name="firstName"
                                    variant="outlined"
                                    fullWidth
                                    id="firstName"
                                    label="First Name"
                                    autoFocus
                                    value={firstName}
                                    onChange={(e) => this.setState({firstName: e.target.value})}
                                />
                            </Grid>}
                            {orgForm === 0 && <Grid item xs={12} sm={6}>
                                <TextField
                                    variant="outlined"
                                    fullWidth
                                    id="lastName"
                                    label="Last Name"
                                    name="lastName"
                                    value={lastName}
                                    onChange={(e) => this.setState({lastName: e.target.value})}
                                />
                            </Grid>}
                            {orgForm === 1 && <Grid item xs={10}>
                                    <TextField
                                        variant="outlined"
                                        fullWidth
                                        id="idno"
                                        label="IDNO"
                                        name="idno"
                                        value={idno}
                                        onChange={(e) => this.setState({idno: e.target.value})}
                                    />
                                </Grid>}
                            {orgForm === 1 && <Grid item xs={2}>
                                <IconButton aria-label="search"
                                            disabled={!idno || searching}
                                            onClick={() => this.findEntity()}
                                >
                                    <SearchIcon fontSize="large"/>
                                </IconButton>
                            </Grid>}
                            <Grid item xs={12}>
                                <TextField
                                    variant="outlined"
                                    required
                                    fullWidth
                                    id="email"
                                    label="Email Address"
                                    name="email"
                                    value={email}
                                    onChange={(e) => this.setState({email: e.target.value})}
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
                                    value={password}
                                    onChange={(e) => this.setState({password: e.target.value})}
                                />
                            </Grid>
                        </Grid>
                        <Button
                            fullWidth
                            variant="contained"
                            color="primary"
                            onClick={() => this.signUp()}
                            className={classes.submit}
                        >
                            Sign Up
                        </Button>
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
