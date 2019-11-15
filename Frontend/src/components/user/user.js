import React, { useState, useEffect, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { UserAction } from '../../store/user/user.action';
import * as mqtt from '../../utils/Mqtt';
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
  const userId = useSelector(state => state.user.id);
  const intervalId = useRef();
  const [isCall, setIsCall] = useState(false);
  const droneCall = () => {
    if (window.navigator.geolocation) setIsCall(true);
    else alert('Current location information is not verified.');
  }

  useEffect(() => {
    mqtt.connect();

    return () => {
      clearInterval(intervalId.current);
    }
  }, []);

  useEffect(() => {
    if (isCall) {
      const interval = setInterval(() => {
        window.navigator.geolocation.getCurrentPosition(position => {
          mqtt.publish('/oneM2M/req/FE_APP/' + userId + '/json', { lat: position.coords.latitude, lon: position.coords.longitude });
        }, () => {
          alert('Current location information is not verified.');
          clearInterval(interval);
        }, { enableHighAccuracy: true });
      }, 1000);
      intervalId.current = interval;
    }
    else clearInterval(intervalId.current);
  }, [isCall]);

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
                  {!isCall
                    ? <Button variant="contained" color="secondary" onClick={() => droneCall()}>
                      Drone Call
                  </Button>
                    : <Button variant="contained" color="secondary" onClick={() => setIsCall(false)}>
                      Terminate Drone Call
                    </Button>
                  }
                </Grid>
                <Grid item>
                  <Button variant="outlined" color="secondary"
                    onClick={() => alert('The service center phone number is not registered yet.')}>
                    Connect Service Center
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