import express, { Request, Response } from "express";
import { takeoff, landing } from '../utils/oneM2M';
import { DroneInfoGrp, patrol, removeDisconnectedUser } from '../utils/infoManager';

const router = express.Router();

router.get('/takeoff/:droneId', function (req: Request, res: Response) {
  const droneId = req.params.droneId.substring(1, req.params.droneId.length);
  takeoff(droneId);
  res.status(200).send();
});

router.get('/landing/:droneId', function (req: Request, res: Response) {
  const droneId = req.params.droneId.substring(1, req.params.droneId.length);
  landing(droneId);
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
    if (droneInfo.target === userId) {
      patrol(droneInfo.id.substring(1, droneInfo.id.length));
      return true;
    }
  });
  removeDisconnectedUser(userId);
  res.status(200).send();
});

export { router };