import express, { Request, Response } from "express";
import { takeoffTest } from '../utils/oneM2M';
import { DroneInfoGrp, patrol } from '../utils/infoManager';

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

router.get('/call/end/:userId', function (req: Request, res: Response) {
  const userId = req.params.userId;
  DroneInfoGrp.forEach(droneInfo => {
    if (droneInfo.status === 'accompany' && droneInfo.target === userId) {
      patrol(droneInfo.id);
      return true;
    }
  })
  res.status(200).send();
});

export { router };