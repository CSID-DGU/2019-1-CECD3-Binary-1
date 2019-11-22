import request from 'request';
import { server } from './Api';
import { HOST_ADDRESS } from './env';

const MOBIUS_URL = `http://${HOST_ADDRESS}:8080`;
const FE_APP_URL = `http://${HOST_ADDRESS}:3000`;

export const init = () => {
  request.post({
    headers: {
      'X-M2M-RI': 'FE_APP',
      'X-M2M-Origin': '/Mobius/FE_APP',
      'Content-Type': 'application/vnd.onem2m-res+json;ty=2',
      'Accept': 'application/json'
    },
    body: {
      'm2m:ae': {
        'rn': 'FE_APP',
        'api': '0.2.481.2.0001.001.000112',
        'rr': true,
        'poa': [FE_APP_URL]
      }
    },
    url: MOBIUS_URL + '/Mobius',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else {
      console.log(body);
      lookupBEApp();
    }
  });
}

const lookupBEApp = () => {
  request.get({
    headers: {
      'X-M2M-RI': 'FE_APP',
      'X-M2M-Origin': '/Mobius/FE_APP',
      'Accept': 'application/json'
    },
    url: MOBIUS_URL + '/Mobius/BE_APP',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else {
      console.log(body);
      server.url = body['m2m:ae']['poa'][0];
    }
  });
}

export const lookupDrone = (droneId) => {
  request.get({
    headers: {
      'X-M2M-RI': 'FE_APP',
      'X-M2M-Origin': '/Mobius/FE_APP',
      'Accept': 'application/json'
    },
    url: MOBIUS_URL + `/Mobius/${droneId}`,
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else {
      console.log(body);
      return body['m2m:ae']['poa'][0];
    }
  });
}