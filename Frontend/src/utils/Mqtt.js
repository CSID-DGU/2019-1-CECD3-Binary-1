import mqtt from 'mqtt';

const client = mqtt.connect('ws://10.0.75.2:9001');

export const connect = () => {
  client.on('connect', function () {
    console.log("Connect success!");
  });

  client.on('message', function (topic, message) {
    console.log(message.toString());
  });
}

export const subscribe = () => {
  client.subscribe('/oneM2M/req/Mobius2/SDrone1/json', function (err) {
    if (err) console.error(err);
    else console.log("Subscribe success!");
  });
}