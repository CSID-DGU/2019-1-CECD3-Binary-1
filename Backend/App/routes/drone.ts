import express, { Request, Response } from "express";
import { takeoff, landing, patrol } from '../utils/oneM2M';
import { DroneInfoGrp, returnToLaunch, matching, removeDisconnectedUser } from '../utils/infoManager';

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
  const droneId = req.params.droneId;
  let drone = DroneInfoGrp.get(droneId) || { id: '', url: '', status: '', target: '' };
  drone.status = 'patrol';
  patrol(droneId.substring(1, droneId.length));
  matching();
  res.status(200).send();
});

router.get('/call/end/:userId', function (req: Request, res: Response) {
  const userId = req.params.userId;
  DroneInfoGrp.forEach(droneInfo => {
    if (droneInfo.target === userId) {
      returnToLaunch(droneInfo.id);
      return true;
    }
  });
  removeDisconnectedUser(userId);
  res.status(200).send();
});

export { router };