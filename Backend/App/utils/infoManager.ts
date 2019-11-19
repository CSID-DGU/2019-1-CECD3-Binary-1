import moment from 'moment';
import 'moment-timezone';
import { publish } from './mqtt';
import { setTargetUserID } from './oneM2M';
import { IUserInfo, IDroneInfo } from '../models/info';
import * as gpsManager from './gpsManager';

moment.tz.setDefault('Asia/Seoul')

export const UserInfoGrp: Map<string, IUserInfo> = new Map();
export const DroneInfoGrp: Map<string, IDroneInfo> = new Map();

export const register = (type: string, id: string, url: string) => {
  if (type === 'user') {
    UserInfoGrp.set(id, { id: id, status: 'unmatched' });
    matching();
  }
  else if (type === 'drone') DroneInfoGrp.set(id, { id: id, url: url, status: '', target: '' });
}

export const patrol = (droneId: string) => {
  let drone = DroneInfoGrp.get(droneId) || { id: '', url: '', status: '', target: '' };
  drone.status = 'patrol';
  drone.target = '';
  setTargetUserID('', droneId);
  matching();
}

const matching = () => {
  UserInfoGrp.forEach(userInfo => {
    if (userInfo.status === 'unmatched') {
      const userGps = gpsManager.UserGpsGrp.get(userInfo.id) || { lat: 0, lon: 0 };
      let droneId, distance = -1;
      DroneInfoGrp.forEach(droneInfo => {
        if (droneInfo.status === 'patrol') {
          const droneGps = gpsManager.DroneGpsGrp.get(droneInfo.id) || { lat: 0, lon: 0 };
          const tmp = calcDistance(userGps.lat, userGps.lon, droneGps.lat, droneGps.lon);
          if (distance == -1 || distance > tmp) {
            droneId = droneInfo.id;
            distance = tmp;
          }
        }
      });
      if (droneId) {
        droneId = droneId || '';
        userInfo.status = 'matched';
        let drone = DroneInfoGrp.get(droneId) || { id: '', url: '', status: '', target: '' };
        drone.status = 'accompany';
        drone.target = userInfo.id;
        setTargetUserID(userInfo.id, droneId.substring(1, length));
      }
    }
  });
}

export const removeDisconnectedUser = (userId: string) => {
  UserInfoGrp.delete(userId);
  gpsManager.remove('user', userId);
  publish('/oneM2M/req/BE_APP/' + userId + '/json', { id: userId, type: 'user' });
}

export const removeDisconnectedDrone = () => {
  gpsManager.DroneGpsGrp.forEach(droneGps => {
    if (moment.duration(moment().diff(droneGps.timestamp)).asSeconds() > 10) {
      DroneInfoGrp.delete(droneGps.id);
      gpsManager.remove('drone', droneGps.id);
      publish('/oneM2M/req/BE_APP/' + droneGps.id + '/json', { id: droneGps.id, type: 'drone' });
    }
  });
}

const calcDistance = (lat1: number, lon1: number, lat2: number, lon2: number) => {
  let theta = Number((lon1 - lon2).toFixed(13));
  let dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
  dist = Math.acos(dist);
  dist = rad2deg(dist);
  dist = dist * 60 * 1.1515;
  dist = dist * 1.609344;
  return Number((dist * 1000).toFixed());
}

const deg2rad = (deg: number) => {
  return (deg * Math.PI / 180);
}
const rad2deg = (rad: number) => {
  return (rad * 180 / Math.PI);
}