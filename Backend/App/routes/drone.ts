import express, { Request, Response } from "express";
import { takeoffTest } from '../utils/oneM2M';
import { patrol } from '../utils/infoManager';

const router = express.Router();

router.get('/takeoff/:droneId', function (req: Request, res: Response) {
  const droneId = req.params.droneId.substring(1, req.params.droneId.length);
  takeoffTest(droneId);
  res.status(200).send();
});

router.get('/patrol/:droneId', function (req: Request, res: Response) {
  const droneId = req.params.droneId.substring(1, req.params.droneId.length);
  patrol(droneId);
  res.status(200).send();
});

export { router };