import request from 'request';
import { oneM2M } from '../models/oneM2M';

export const init = () => {
  request.post({
    headers: {
      'X-M2M-RI': 'BE_APP',
      'X-M2M-Origin': '/Mobius/BE_APP',
      'Content-Type': 'application/vnd.onem2m-res+json;ty=2',
      'Accept': 'application/json'
    },
    body: {
      'm2m:ae': {
        'rn': 'BE_APP',
        'api': '0.2.481.2.0001.001.000111',
        'rr': true
      }
    },
    url: 'http://10.0.75.2:8080/Mobius',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else {
      console.log(body);
      lookup_rosemary();
    }
  });
}

const lookup_rosemary = () => {
  request.get({
    headers: {
      'X-M2M-RI': 'BE_APP',
      'X-M2M-Origin': '/Mobius/BE_APP',
      'Accept': 'application/json'
    },
    url: 'http://10.0.75.2:8080/Mobius/Rosemary',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else {
      console.log(body);
      oneM2M.Rosemary.url = body['m2m:csr']['poa'][0];
      lookup_drones();
    }
  });
}

const lookup_drones = () => {
  request.get({
    headers: {
      'X-M2M-RI': 'BE_APP',
      'X-M2M-Origin': '/Mobius/BE_APP',
      'Accept': 'application/json'
    },
    url: 'http://10.0.75.2:8090/Rosemary/GW_APP/gpsGrp',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else {
      console.log(body['m2m:grp']['mid']);
      body['m2m:grp']['mid'].map((drone: string) => {
        oneM2M.Drone_Grp.push({ id: drone, url: '' })
      })
    }
  });
}