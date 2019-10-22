import React from 'react';
import { useDispatch } from 'react-redux';
import { UserAction } from '../../store/user/user.action';
import CityMap from '../map';
import Player from '../player';
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
import { grey } from '@material-ui/core/colors'
import { Dashboard } from '@material-ui/icons';

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

const cameras = [
  { id: 'camera1', url: 'ws://localhost:9999/' },
  { id: 'camera2', url: 'ws://localhost:9998/' },
  { id: 'camera1', url: 'ws://localhost:9999/' },
  { id: 'camera2', url: 'ws://localhost:9998/' },
  { id: 'camera1', url: 'ws://localhost:9999/' },
];

const Admin = () => {
  const classes = useStyles();
  const dispatch = useDispatch();

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
            <CityMap />
          </Container>
        </div>
        <Container className={classes.cardGrid} maxWidth="xl">
          <Grid container spacing={4} xs="12">
            {cameras.map((camera, index) => (
              <Grid item key={camera.id} xs="6">
                <Card className={classes.card}>
                  <Player index={index} url={camera.url} />
                  <CardContent className={classes.cardContent}>
                    <Typography gutterBottom variant="h5" component="h2">
                      {camera.id}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Container>
      </main>
    </React.Fragment>
  );
}

export default Admin;