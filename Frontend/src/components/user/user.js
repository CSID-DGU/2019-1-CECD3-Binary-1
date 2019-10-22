import React from 'react';
import { useDispatch } from 'react-redux';
import { UserAction } from '../../store/user/user.action';
import {
  AppBar,
  Button,
  CssBaseline,
  Grid,
  Link,
  Toolbar,
  Typography,
  makeStyles,
  Container,
} from '@material-ui/core';
import { grey } from '@material-ui/core/colors'
import { Policy } from '@material-ui/icons';

const useStyles = makeStyles(theme => ({
  root: {
    display: "flex",
    minHeight: "100vh",
    flexDirection: "column",
    backgroundColor: theme.palette.background.paper,
  },
  toolbar: {
    background: grey[900],
  },
  icon: {
    marginRight: theme.spacing(2),
  },
  toolbarButtons: {
    marginLeft: "auto",
    marginRight: theme.spacing(1),
  },
  logoutButton: {
    background: grey[200],
  },
  heroContent: {
    padding: theme.spacing(8, 0, 6),
  },
  heroButtons: {
    marginTop: theme.spacing(4),
  },
  footer: {
    position: "absolute",
    bottom: "0",
    width: "100%",
    padding: theme.spacing(2),
  },
}));

const Copyright = () => {
  return (
    <Typography variant="body2" color="textSecondary" align="center">
      {'Copyright © '}
      <Link color="inherit" href="https://github.com/CSID-DGU/2019-1-CECD3-Binary-1">
        Dongguk Univ. CECD Team Binary
      </Link>{' '}
      {new Date().getFullYear()}
      {'.'}
    </Typography>
  );
}

const User = () => {
  const classes = useStyles();
  const dispatch = useDispatch();

  return (
    <div className={classes.root}>
      <CssBaseline />
      <AppBar position="relative">
        <Toolbar className={classes.toolbar}>
          <Policy className={classes.icon} />
          <Typography variant="h6" color="inherit" noWrap>
            Smart Policing Service
          </Typography>
          <span className={classes.toolbarButtons}>
            <Button
              variant="contained"
              className={classes.logoutButton}
              onClick={() => dispatch(UserAction.logout.index())}
            >
              Logout
          </Button>
          </span>
        </Toolbar>
      </AppBar>
      <main>
        <div className={classes.heroContent}>
          <Container maxWidth="sm">
            <Typography component="h1" variant="h3" align="center" color="textPrimary" gutterBottom>
              About smart policing
            </Typography>
            <Typography variant="h5" align="center" color="textSecondary" paragraph>
              Something short and leading about the collection below—its contents, the creator, etc.
              Make it short and sweet, but not too short so folks don&apos;t simply skip over it
              entirely.
            </Typography>
            <div className={classes.heroButtons}>
              <Grid container spacing={2} justify="center">
                <Grid item>
                  <Button variant="contained" color="secondary" onClick={() => alert('Drone Call')}>
                    Drone Call
                  </Button>
                </Grid>
                <Grid item>
                  <Button variant="outlined" color="secondary"
                    onClick={() => alert('The service center phone number is not registered yet.')}>
                    Service Center Connection
                  </Button>
                </Grid>
              </Grid>
            </div>
          </Container>
        </div>
        <footer className={classes.footer}>
          <Container maxWidth="sm">
            <Typography variant="body1">Smart Policing System using Drone based on oneM2M Standard.</Typography>
            <Copyright />
          </Container>
        </footer>
      </main>
    </div>
  );
}

export default User;