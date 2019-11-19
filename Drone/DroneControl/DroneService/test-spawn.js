var net = require('net');

var client = net.connect({path: '/tmp/unix.sock'},function() { // UNIX domain sockets  사용시
    console.log('connected to server!');
    var data  = 'call_patrol';
    console.log('serve send data : '+data);
    client.write(data);
});

//서버로 부터 받은 데이터
client.on('data', function(data) {
    console.log('serve get data : '+data.toString());
    client.end();
});

client.on('end', function() {
    console.log('disconnected from server');
});

client.on('error', function(err) {
    console.log(err);
});