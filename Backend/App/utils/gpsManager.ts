import moment from 'moment';
import 'moment-timezone';
import { IUserGps, IDroneGps } from '../models/gps';

moment.tz.setDefault('Asia/Seoul')

export const UserGpsGrp: Map<string, IUserGps> = new Map();
export const DroneGpsGrp: Map<string, IDroneGps> = new Map();

export const update = (type: string, id: string, lat: number, lon: number) => {
  const timestamp = moment().format('YYYY-MM-DD HH:mm:ss');
  if (type === 'user') UserGpsGrp.set(id, { id: id, lat: lat, lon: lon, timestamp: timestamp });
  else if (type === 'drone') DroneGpsGrp.set(id, { id: id, lat: lat, lon: lon, timestamp: timestamp });
}

export const remove = (type: string, id: string) => {
  if (type === 'user') UserGpsGrp.delete(id);
  else if (type === 'drone') DroneGpsGrp.delete(id);
}