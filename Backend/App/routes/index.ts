import express, { Request, Response } from "express";
import { User, IUser } from '../models/user';

const router = express.Router();

router.post('/login', function (req: Request, res: Response) {
  const { id, pw } = req.body.data
  User.findOne({ 'id': id }, function (err, result: IUser) {
    if (err) res.status(500).send(err);
    else {
      if (result != null && pw == result.pw) res.status(200).send({ auth: result.auth, name: result.name });
      else res.status(200).send();
    }
  });
});

export { router };