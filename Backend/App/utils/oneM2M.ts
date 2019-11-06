import request from 'request';

const MOBIUS_URL = 'http://10.0.75.2:8080';
const BE_APP_URL = 'http://10.0.75.2:8081';

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
    else {
      console.log(body);
      create_gpsGrp();
    }
  });
}

const create_gpsGrp = () => {
  request.post({
    headers: {
      'X-M2M-RI': 'GW_APP',
      'X-M2M-Origin': '/Mobius/BE_APP',
      'Content-Type': 'application/vnd.onem2m-res+json;ty=9',
      'Accept': 'application/json'
    },
    body: {
      'm2m:grp': {
        'rn': 'gpsGrp',
        'mnm': 10,
        'mid': []
      }
    },
    url: MOBIUS_URL + '/Mobius/BE_APP',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else {
      console.log(body);
      update_gpsGrp();
    }
  });
}

const update_gpsGrp = () => {
  request.put({
    headers: {
      'X-M2M-RI': 'GW_APP',
      'X-M2M-Origin': '/Mobius/BE_APP',
      'Content-Type': 'application/vnd.onem2m-res+json',
      'Accept': 'application/json'
    },
    body: {
      'm2m:grp': {
        'mnm': 100,
        'mid': ['/Mobius/Drone0/gps', '/Mobius/Drone1/gps']
      }
    },
    url: MOBIUS_URL + '/Mobius/BE_APP/gpsGrp',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else console.log(body);
  });
}

const lookup_drones = () => {
  request.get({
    headers: {
      'X-M2M-RI': 'BE_APP',
      'X-M2M-Origin': '/Mobius/BE_APP',
      'Accept': 'application/json'
    },
    url: MOBIUS_URL + '/Mobius?rcn=4',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else {
      console.log(body['m2m:rsp']['m2m:ae']);
      console.log(body['m2m:rsp']['m2m:grp']);
      // console.log(body['m2m:grp']['mid']);
      // body['m2m:grp']['mid'].map((drone: string) => {
      //   oneM2M.Drone_Grp.push({ id: drone, url: '' })
      // })
    }
  });
}