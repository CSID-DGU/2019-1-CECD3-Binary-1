import React, { useState, useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { UserAction } from '../../store/user/user.action';
import CityMap from '../map';
import Player from '../player';
import { GET } from '../../utils/Api';
import * as mqtt from '../../utils/Mqtt';
import moment from 'moment';
import 'moment-timezone';
import {
  AppBar,
  Button,
  Card,
  CardContent,
  CssBaseline,
  Grid,
  Toolbar,
  Typography,
  makeStyles,
  Container,
} from '@material-ui/core';
import { grey } from '@material-ui/core/colors';
import { Dashboard } from '@material-ui/icons';

moment.tz.setDefault('Asia/Seoul')

const useStyles = makeStyles(theme => ({
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
    backgroundColor: theme.palette.background.paper,
    padding: theme.spacing(1, 0, 0),
  },
  heroButtons: {
    marginTop: theme.spacing(4),
  },
  testButtons: {
    textDecorationLine: 'underline',
    margin: theme.spacing(0, 4),
  },
  cardGrid: {
    paddingTop: theme.spacing(8),
    paddingBottom: theme.spacing(8),
  },
  card: {
    height: '100%',
    width: '100%',
    display: 'flex',
    flexDirection: 'column',
  },
  cardContent: {
    flexGrow: 1,
    textAlign: 'center',
    textTransform: 'uppercase',
  },
}));

export const cameraGrp = new Map();

const Admin = () => {
  const classes = useStyles();
  const dispatch = useDispatch();
  const [timestamp, setTimestamp] = useState('');
  const [userGrp, setUserGrp] = useState(new Map());
  const [droneGrp, setDroneGrp] = useState(new Map());
  const updateUserGps = (id, lat, lon) => {
    setUserGrp(userGrp.set(id, { id: id, lat: lat, lon: lon }));
    setTimestamp(moment().format('YYYY-MM-DD HH:mm:ss'));
  };
  const removeUser = (id) => {
    userGrp.delete(id);
    setUserGrp(userGrp);
    setTimestamp(moment().format('YYYY-MM-DD HH:mm:ss'));
  };
  const updateDroneGps = (id, lat, lon) => {
    setDroneGrp(droneGrp.set(id, { id: id, lat: lat, lon: lon }));
    setTimestamp(moment().format('YYYY-MM-DD HH:mm:ss'));
  };
  const removeDrone = (id) => {
    droneGrp.delete(id);
    setDroneGrp(droneGrp);
    setTimestamp(moment().format('YYYY-MM-DD HH:mm:ss'));
  };

  useEffect(() => {
    mqtt.connect(updateUserGps, removeUser, updateDroneGps, removeDrone);
  }, []);

  const cameras = [];

  cameraGrp.forEach((camera, index) => cameras.push(
    <Grid item key={camera.id} xs="6">
      <Card className={classes.card}>
        <Player index={index} url={camera.url} />
        <CardContent className={classes.cardContent}>
          <Typography gutterBottom variant="h5" component="h2">
            {camera.id.substring(1, camera.id.length)}
          </Typography>
          <Button
            className={classes.testButtons}
            onClick={() => GET(`/drones/takeoff/${camera.id}`)}
          >
            Take-off
            </Button>
          <Button
            className={classes.testButtons}
            onClick={() => GET(`/drones/landing/${camera.id}`)}
          >
            Landing
            </Button>
          <Button
            className={classes.testButtons}
            onClick={() => GET(`/drones/patrol/${camera.id}`)}
          >
            Patrol Start
            </Button>
        </CardContent>
      </Card>
    </Grid>
  ))

  return (
    <React.Fragment>
      <CssBaseline />
      <AppBar position="relative">
        <Toolbar className={classes.toolbar}>
          <Dashboard className={classes.icon} />
          <Typography variant="h6" color="inherit" noWrap>
            Smart Policing Control Board
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
          <Container maxWidth="xl">
            <CityMap users={userGrp} drones={droneGrp} />
          </Container>
        </div>
        <Container className={classes.cardGrid} maxWidth="xl">
          <Grid container spacing={4} xs="12">
            {cameras}
          </Grid>
        </Container>
      </main>
    </React.Fragment>
  );
}

export default Admin;