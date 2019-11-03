var child = require('child_process').execFile('./takeoff_and_land', [ 
    'serial:///dev/ttyS0:921600' ]); 
// use event hooks to provide a callback to execute when data are available: 
child.stdout.on('data', function(data) {
    console.log(data.toString()); 
});