import mqtt from 'mqtt';

const client = mqtt.connect('ws://10.0.75.2:9001');

export const connect = () => {
  client.on('connect', function () {
    console.log("Connect success!");
  });
}

export const on = (updateUserGps, updateDroneGps) => {
  client.on('message', function (topic, message) {
    let data = JSON.parse(message.toString());
    
    if (topic.split('/')[3] === 'FE_APP')
      updateUserGps(data.id, data.lat, data.lon);
    else if (topic.split('/')[3] === 'Mobius2')
      updateDroneGps(data.id, data.lat, data.lon);
  });
}

export const subscribe = () => {
  client.subscribe('/oneM2M/req/FE_APP/id/json', function (err) {
    if (err) console.error(err);
    else console.log("Subscribe success!");
  });

  client.subscribe('/oneM2M/req/Mobius2/Test/json', function (err) {
    if (err) console.error(err);
    else console.log("Subscribe success!");
  });
}

export const publish = (topic, message) => {
  console.log(topic, message)
  client.publish(topic, JSON.stringify(message));
}