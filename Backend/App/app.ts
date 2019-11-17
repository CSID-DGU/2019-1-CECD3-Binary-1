import express, { Request, Response, NextFunction } from 'express';
import session from 'express-session';
import bodyParser from 'body-parser';
import mongo from 'connect-mongo';
import mongoose from 'mongoose';
import * as onem2m from './utils/oneM2M';
import * as mqtt from './utils/mqtt';
import { MONGODB_URL, SESSION_SECRET } from './utils/env';

const app = express();
const MongoStore = mongo(session);

// Connect to MongoDB
mongoose.Promise = global.Promise;
mongoose.connect(MONGODB_URL, { useNewUrlParser: true })
  .then(() => {
    console.log('Successfully connected to MongoDB.');
  })
  .catch(err => {
    console.log('MongoDB connection error. Please make sure MongoDB is running. ' + err);
    process.exit();
  });

// Express configuration
app.set('port', 8081);
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(session({
  resave: true,
  saveUninitialized: true,
  secret: SESSION_SECRET,
  store: new MongoStore({
    url: MONGODB_URL,
    autoReconnect: true
  })
}));
app.use(function (req: Request, res: Response, next: NextFunction) {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE');
  next();
});

// Route handlers
import * as index from './routes/index';
import * as user from './routes/user';

app.use('/', index.router);
app.use('/users', user.router);

// Start server
app.listen(app.get('port'), () => {
  console.log('App is running at http://localhost:%d in %s mode', app.get('port'), app.get('env'));
  onem2m.init();
  mqtt.connect();
});