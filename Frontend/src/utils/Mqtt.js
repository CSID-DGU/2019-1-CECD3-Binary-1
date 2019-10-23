import mqtt from 'mqtt';

export const mqttConnection = (rosemaryUrl) => {
  const mqttUrl = /([a-z0-9\w]+\.)+([a-z0-9\w]+)/g.exec(rosemaryUrl)[0]
  const client = mqtt.connect('ws://' + mqttUrl + ':9001');

  client.on('connect', function () {
    client.subscribe('/oneM2M/req/Rosemary2/SDrone1/json', function (err) {
      if (err) console.error(err);
      else console.log("Subscribe success!");
    });
  });

  client.on('message', function (topic, message) {
    console.log(message.toString());
  });
}
