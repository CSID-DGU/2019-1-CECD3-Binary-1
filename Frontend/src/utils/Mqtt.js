import mqtt from 'mqtt';

const client = mqtt.connect('ws://10.0.75.2:9001');

export const connect = () => {
  client.on('connect', function () {
    console.log("Connect success!");
  });
}

export const on = (updateDroneGps) => {
  client.on('message', function (topic, message) {
    let data = JSON.parse(message.toString());
    updateDroneGps(data.id, data.lat, data.lon);
  });
}

export const subscribe = () => {
  client.subscribe('/oneM2M/req/Mobius2/Test/json', function (err) {
    if (err) console.error(err);
    else console.log("Subscribe success!");
  });
}