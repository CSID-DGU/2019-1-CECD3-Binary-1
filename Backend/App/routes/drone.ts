import express, { Request, Response } from "express";
import { patrol } from '../utils/infoManager';

const router = express.Router();

router.get('/patrol/:droneId', function (req: Request, res: Response) {
  patrol(req.params.droneId);
  res.status(200).send();
});

export { router };