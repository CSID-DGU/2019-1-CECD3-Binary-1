import mqtt from 'mqtt';

const client = mqtt.connect('ws://10.0.75.2:9001');

export const connect = (updateUserGps, removeUser, updateDroneGps, removeDrone) => {
  client.on('connect', function () {
    client.subscribe('/oneM2M/req/FE_APP/+/json', function (err) {
      if (err) console.error(err);
      else console.log("Subscribe FE_APP success!");
    });
    client.subscribe('/oneM2M/req/Mobius2/+/json', function (err) {
      if (err) console.error(err);
      else console.log("Subscribe Mobius2 success!");
    });
    client.subscribe('/oneM2M/req/BE_APP/+/json', function (err) {
      if (err) console.error(err);
      else console.log("Subscribe BE_APP success!");
    });
    client.on('message', function (topic, message) {
      const data = JSON.parse(message.toString());
      if (topic.split('/')[3] === 'FE_APP')
        updateUserGps(data.id, data.lat, data.lon);
      else if (topic.split('/')[3] === 'Mobius2')
        updateDroneGps(data.id, data.lat, data.lon);
      else if (topic.split('/')[3] === 'BE_APP') {
        if (data.type === 'user') removeUser(data.id, data.lat, data.lon);
        else if (data.type === 'drone') removeDrone(data.id, data.lat, data.lon);
      }
    });
  });
}

export const publish = (topic, message) => {
  console.log(topic, message)
  client.publish(topic, JSON.stringify(message));
}