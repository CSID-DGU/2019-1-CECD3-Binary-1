import express, { Request, Response, NextFunction } from "express";
import bodyParser from "body-parser";
import * as onem2m from './utils/oneM2M';

const app = express();

// Express configuration
app.set("port", process.env.PORT || 4000);
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
import * as user from './routes/user';

app.use('/', index.router);
app.use('/users', user.router);

// Start server
app.listen(app.get('port'), () => {
  console.log("App is running at http://localhost:%d in %s mode", app.get("port"), app.get("env"));
  onem2m.init();
});