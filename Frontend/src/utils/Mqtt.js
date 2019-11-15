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
      const id = topic.split('/')[4];
      const data = JSON.parse(message.toString());
      if (topic.split('/')[3] === 'FE_APP')
        updateUserGps(id, parseFloat(data.lat), parseFloat(data.lon));
      else if (topic.split('/')[3] === 'Mobius2')
        try {
          const con = data['pc']['m2m:sgn']['nev']['rep']['m2m:cin']['con'];
          updateDroneGps(id, parseFloat(con.latitude), parseFloat(con.longitude));
        } catch (error) { /* No error handling required */ }
      else if (topic.split('/')[3] === 'BE_APP') {
        if (data.type === 'user') removeUser(data.id);
        else if (data.type === 'drone') removeDrone(data.id);
      }
    });
  });
}

export const publish = (topic, message) => {
  console.log(topic, message)
  client.publish(topic, JSON.stringify(message));
}