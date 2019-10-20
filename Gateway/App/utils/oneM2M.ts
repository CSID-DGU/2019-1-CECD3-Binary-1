import request from 'request';

export const init = () => {
  request.post({
    headers: {
      'X-M2M-RI': 'GW_APP',
      'X-M2M-Origin': '/Mobius/Rosemary/GW_APP',
      'Content-Type': 'application/vnd.onem2m-res+json;ty=2',
      'Accept': 'application/json'
    },
    body: {
      'm2m:ae': {
        'rn': 'GW_APP',
        'api': '0.2.481.2.0001.001.000111',
        'rr': true
      }
    },
    url: 'http://10.0.75.2:8090/Rosemary',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else {
      console.log(body);
      // create_gpsGrp();
      // update_gpsGrp();
      // create_urlGrp();
      // update_urlGrp();
      lookup_rosemary();
    }
  });
}

const create_gpsGrp = () => {
  request.post({
    headers: {
      'X-M2M-RI': 'GW_APP',
      'X-M2M-Origin': '/Mobius/Rosemary/GW_APP',
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
    url: 'http://10.0.75.2:8090/Rosemary/GW_APP',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else console.log(body);
  });
}

const update_gpsGrp = () => {
  request.put({
    headers: {
      'X-M2M-RI': 'GW_APP',
      'X-M2M-Origin': '/Mobius/Rosemary/GW_APP',
      'Content-Type': 'application/vnd.onem2m-res+json',
      'Accept': 'application/json'
    },
    body: {
      "m2m:grp": {
        "mnm": 100,
        'mid': ['/Mobius/Rosemary/Drone0/gps', '/Mobius/Rosemary/Drone1/gps']
      }
    },
    url: 'http://10.0.75.2:8090/Rosemary/GW_APP/gpsGrp',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else console.log(body);
  });
}

const create_urlGrp = () => {
  request.post({
    headers: {
      'X-M2M-RI': 'GW_APP',
      'X-M2M-Origin': '/Mobius/Rosemary/GW_APP',
      'Content-Type': 'application/vnd.onem2m-res+json;ty=9',
      'Accept': 'application/json'
    },
    body: {
      'm2m:grp': {
        'rn': 'urlGrp',
        'mnm': 10,
        'mid': []
      }
    },
    url: 'http://10.0.75.2:8090/Rosemary/GW_APP',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else console.log(body);
  });
}

const update_urlGrp = () => {
  request.put({
    headers: {
      'X-M2M-RI': 'GW_APP',
      'X-M2M-Origin': '/Mobius/Rosemary/GW_APP',
      'Content-Type': 'application/vnd.onem2m-res+json',
      'Accept': 'application/json'
    },
    body: {
      "m2m:grp": {
        "mnm": 100,
        'mid': ['/Mobius/Rosemary/Drone0/url', '/Mobius/Rosemary/Drone1/url']
      }
    },
    url: 'http://10.0.75.2:8090/Rosemary/GW_APP/urlGrp',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else console.log(body);
  });
}

const lookup_rosemary = () => {
  request.get({
    headers: {
      'X-M2M-RI': 'GW_APP',
      'X-M2M-Origin': '/Mobius/Rosemary/GW_APP',
      'Accept': 'application/json'
    },
    url: 'http://10.0.75.2:8090/Rosemary/Drone1?rcn=4',
    json: true
  }, function (error, res, body) {
    if (error) console.error(error);
    else console.log(body['m2m:rsp']['m2m:cnt']);
  });
}