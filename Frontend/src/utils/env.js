import dotenv from 'dotenv';

dotenv.config({ path: '2019-1-CECD3-Binary-1.env' });

export const HOST_ADDRESS = process.env['REACT_APP_HOST_ADDRESS'] || 'error';
export const GOOGLE_API_KEY = process.env['REACT_APP_GOOGLE_API_KEY'] || 'error';

if (HOST_ADDRESS === 'error') console.error('No host server address. Set REACT_APP_HOST_ADDRESS environment variable.');
if (GOOGLE_API_KEY === 'error') console.error('No google api key. Set REACT_APP_GOOGLE_API_KEY environment variable.');