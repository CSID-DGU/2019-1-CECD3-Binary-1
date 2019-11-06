import moment from 'moment';
import 'moment-timezone';
import { publish } from './mqtt';
import { IUserInfo, IDroneInfo } from '../models/info';
import * as gpsManager from './gpsManager';

moment.tz.setDefault('Asia/Seoul')

export const UserInfoGrp: Map<string, IUserInfo> = new Map();
export const DroneInfoGrp: Map<string, IDroneInfo> = new Map();

export const register = (type: string, id: string, url: string) => {
  if (type === 'user') UserInfoGrp.set(id, { id: id });
  else if (type === 'drone') DroneInfoGrp.set(id, { id: id, url: url });
}

export const remove = () => {
  gpsManager.UserGpsGrp.forEach(userGps => {
    if (moment.duration(moment().diff(userGps.timestamp)).asSeconds() > 10) {
      UserInfoGrp.delete(userGps.id);
      gpsManager.remove('user', userGps.id);
      publish('/oneM2M/req/BE_APP/' + userGps.id + '/json', { id: userGps.id, type: 'user' });
    }
  });
  gpsManager.DroneGpsGrp.forEach(droneGps => {
    if (moment.duration(moment().diff(droneGps.timestamp)).asSeconds() > 10) {
      DroneInfoGrp.delete(droneGps.id);
      gpsManager.remove('drone', droneGps.id);
      publish('/oneM2M/req/BE_APP/' + droneGps.id + '/json', { id: droneGps.id, type: 'drone' });
    }
  });
}