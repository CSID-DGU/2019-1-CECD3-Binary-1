import mqtt from 'mqtt';
import gpxParse from 'gpx-parse';

const droneId = 'drone0'
const client = mqtt.connect('mqtt://10.0.75.2:1883');
const GPS_PATH = [];

const PublishCurrentLoaction = () => {
  let i = 0;
  setInterval(() => {
    if (i == GPS_PATH.length) i = 0;
    client.publish('/oneM2M/req/Mobius2/Test/json', JSON.stringify({ id: droneId, lat: GPS_PATH[i++].lat, lon: GPS_PATH[i++].lon }));
  }, 1000);
}

client.on('connect', function () {
  gpxParse.parseGpxFromFile('./path/test.gpx', (error, data) => {
    if (error) console.error(error);
    else {
      for (let i = 0; i < data.tracks[0].segments[0].length - 1; i++) {
        let lat1 = parseFloat(data.tracks[0].segments[0][i].lat);
        let lon1 = parseFloat(data.tracks[0].segments[0][i].lon);
        let lat2 = parseFloat(data.tracks[0].segments[0][i + 1].lat);
        let lon2 = parseFloat(data.tracks[0].segments[0][i + 1].lon);
        let dlat = lat1 - lat2;
        let dlon = lon1 - lon2;
        let requiredTime = parseInt(calcDistance(lat1, lon1, lat2, lon2));

        for (let j = 0; j < requiredTime; j++) {
          GPS_PATH.push({ lat: lat1 + dlat * j, lon: lon1 + dlon * j });
        }
      }
      PublishCurrentLoaction();
    }
  });
});

function calcDistance(lat1, lon1, lat2, lon2) {
  var theta = lon1 - lon2;
  let dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
    * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
  dist = Math.acos(dist);
  dist = rad2deg(dist);
  dist = dist * 60 * 1.1515;
  dist = dist * 1.609344;
  return Number(dist * 1000).toFixed(2);
}

function deg2rad(deg) {
  return (deg * Math.PI / 180);
}
function rad2deg(rad) {
  return (rad * 180 / Math.PI);
}