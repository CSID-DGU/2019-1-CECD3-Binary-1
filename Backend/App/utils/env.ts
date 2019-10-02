import dotenv from "dotenv";

dotenv.config({ path: ".env" });

export const MONGODB_URI = process.env['MONGODB_URI'] || 'error';
export const SESSION_SECRET = process.env['SESSION_SECRET'] || 'error';

if (MONGODB_URI == 'error') {
  console.error('No mongo connection string. Set MONGODB_URI environment variable.');
  process.exit(1);
}

if (SESSION_SECRET == 'error') {
  console.error('No client secret. Set SESSION_SECRET environment variable.');
  process.exit(1);
}