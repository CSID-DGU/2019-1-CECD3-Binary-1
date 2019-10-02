import mongoose from "mongoose";

export interface IUser {
  id: string,
  pw: string,
  name: string
}

const userSchema = new mongoose.Schema({
  id: { type: String, required: true, unique: true },
  pw: { type: String, required: true },
  name: { type: String, required: true }
});

export const User = mongoose.model('User', userSchema);