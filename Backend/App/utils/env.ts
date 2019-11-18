export const HOST_ADDRESS = process.env['HOST_ADDRESS'] || 'error';
export const MONGODB_URL = process.env['MONGODB_URL'] || 'error';
export const SESSION_SECRET = process.env['SESSION_SECRET'] || 'error';

if (HOST_ADDRESS === 'error') {
  console.error('No host server address. Set HOST_ADDRESS environment variable.');
  process.exit(1);
}

if (MONGODB_URL === 'error') {
  console.error('No mongo connection string. Set MONGODB_URL environment variable.');
  process.exit(1);
}

if (SESSION_SECRET === 'error') {
  console.error('No client secret. Set SESSION_SECRET environment variable.');
  process.exit(1);
}