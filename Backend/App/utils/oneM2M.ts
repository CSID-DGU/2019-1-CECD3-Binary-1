import request from 'request';
import { HOST_ADDRESS } from './env';

const MOBIUS_URL = `http://${HOST_ADDRESS}:8080`;
const BE_APP_URL = `http://${HOST_ADDRESS}:8081`;

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
        'rr': true,
        'poa': [BE_APP_URL]
      }
    },
    url: MOBIUS_URL + '/Mobius',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else console.log(body);
  });
}

export const setTargetUserID = (userID: string, droneID: string) => {
  request.post({
    headers: {
      'X-M2M-RI': 'BE_APP',
      'X-M2M-Origin': '/Mobius/BE_APP',
      'Content-Type': 'application/vnd.onem2m-res+json;ty=4',
      'Accept': 'application/json'
    },
    body: {
      'm2m:cin': {
        'con': userID
      }
    },
    url: MOBIUS_URL + `/Mobius/${droneID}/userId`,
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else {
      console.log(body);
      if (userID === '') droneCall(droneID, 0);
      else droneCall(droneID, 1);
    }
  });
}

const droneCall = (droneID: string, call: number) => {
  request.post({
    headers: {
      'X-M2M-RI': 'BE_APP',
      'X-M2M-Origin': '/Mobius/BE_APP',
      'Content-Type': 'application/vnd.onem2m-res+json;ty=4',
      'Accept': 'application/json'
    },
    body: {
      'm2m:cin': {
        'con': call
      }
    },
    url: MOBIUS_URL + `/Mobius/${droneID}/call`,
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else console.log(body);
  });
}