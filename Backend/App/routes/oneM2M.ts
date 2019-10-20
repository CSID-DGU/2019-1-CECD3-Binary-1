import express, { Request, Response } from "express";
import { oneM2M } from '../models/oneM2M';

const router = express.Router();

router.get('/rosemary', function (req: Request, res: Response) {
  console.log(oneM2M);
  res.status(200).send(oneM2M.Rosemary);
});

router.get('/gwApp', function (req: Request, res: Response) {
  res.status(200).send(oneM2M.GW_APP);
});

router.get('/droneGrp', function (req: Request, res: Response) {
  res.status(200).send(oneM2M.Drone_Grp);
});

export { router };