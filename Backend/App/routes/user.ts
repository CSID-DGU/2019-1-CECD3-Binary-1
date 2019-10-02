import express, { Request, Response } from "express";
import { User } from '../models/user';

const router = express.Router();

// Get all users' data
router.get('/', function (req: Request, res: Response) {
  User.find({}, function (err, result) {
    if (err) res.status(500).send(err);
    else res.status(200).send(result);
  });
});

// Get user's data
router.get('/:id', function (req: Request, res: Response) {
  User.findOne({ 'id': req.params.id }, function (err, result) {
    if (err) res.status(500).send(err);
    else res.status(200).send(result);
  });
});

// Sign Up
router.post('/', function (req: Request, res: Response) {
  User.create({ ...req.body.data, auth: 'user' })
    .then(user => res.status(200).send(user))
    .catch(err => res.status(500).send(err));
});

router.post('/admin', function (req: Request, res: Response) {
  User.create({ ...req.body.data, auth: 'admin' })
    .then(user => res.status(200).send(user))
    .catch(err => res.status(500).send(err));
});

router.put('/:id', function (req: Request, res: Response) {
  User.updateOne({ id: req.params.id }, { pw: req.body.data.pw }, function (err, result) {
    if (err) res.status(500).send(err);
    else res.status(200).send(result);
  })
});

router.delete('/:id', function (req: Request, res: Response) {
  User.deleteOne({ id: req.params.id }, function (err) {
    if (err) res.status(500).send(err);
    else res.status(200);
  });
});

export { router };