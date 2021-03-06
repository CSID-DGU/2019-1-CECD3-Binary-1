import mqtt from 'mqtt';
import * as infoManager from './infoManager';
import * as gpsManager from './gpsManager';
import { HOST_ADDRESS } from './env';

const client = mqtt.connect(`mqtt://${HOST_ADDRESS}:1883`);

export const connect = () => {
  client.on('connect', () => {
    client.subscribe('/oneM2M/req/FE_APP/+/json', err => {
      if (err) console.error(err);
      else console.log("Subscribe success!");
    });

    client.subscribe('/oneM2M/req/Mobius2/+/json', err => {
      if (err) console.error(err);
      else console.log("Subscribe success!");
    });

    client.on('message', (topic, message) => {
      const id = topic.split('/')[4];
      const data = JSON.parse(message.toString());

      if (topic.split('/')[3] === 'FE_APP') {
        if (!infoManager.UserInfoGrp.has(id)) infoManager.register('user', id, '');
        const lat = data.lat;
        const lon = data.lon;
        gpsManager.update('user', id, lat, lon);
      }
      else if (topic.split('/')[3] === 'Mobius2') {
        try {
          const con = data['pc']['m2m:sgn']['nev']['rep']['m2m:cin']['con'];
          gpsManager.update('drone', id, con.latitude, con.longitude);
        } catch (error) {
          if (!infoManager.DroneInfoGrp.has(id)) {
            const url = '';
            infoManager.register('drone', id, url);
          }
        }
      }
    });

    setInterval(() => infoManager.removeDisconnectedDrone(), 10000);
  });
}

export const publish = (topic: string, message: { id: string, type: string }) => {
  console.log(topic, message)
  client.publish(topic, JSON.stringify(message));
}