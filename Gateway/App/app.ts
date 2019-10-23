import express, { Request, Response, NextFunction } from "express";
import mqtt from 'mqtt';
import bodyParser from "body-parser";
import * as onem2m from './utils/oneM2M';

const app = express();
const client = mqtt.connect('mqtt://10.0.75.2:1883');

// Express configuration
app.set("port", 8091);
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(function (req: Request, res: Response, next: NextFunction) {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE');
  next();
});

// Route handlers
import * as index from './routes/index';

app.use('/', index.router);

// Start server
app.listen(app.get('port'), () => {
  console.log("App is running at http://localhost:%d in %s mode", app.get("port"), app.get("env"));
  onem2m.init();
});

client.on('connect', function () {
  client.subscribe('/oneM2M/req/Rosemary2/SDrone1/json', function (err) {
    if (err) console.error(err);
    else console.log("Subscribe success!");
  });
});

client.on('message', function (topic, message) {
  console.log(message.toString());
});