/**
 * Copyright (c) 2018, KETI
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @file Main code of Mobius. Role of flow router
 * @copyright KETI Korea 2018, KETI
 * @author Il Yeup Ahn [iyahn@keti.re.kr]
 */

process.env.NODE_ENV = 'production';
//process.env.NODE_ENV = 'development';

var fs = require('fs');
var http = require('http');
var express = require('express');
var bodyParser = require('body-parser');
var morgan = require('morgan');
var util = require('util');
var xml2js = require('xml2js');
var url = require('url');
var xmlbuilder = require('xmlbuilder');
var ip = require('ip');
var crypto = require('crypto');
var fileStreamRotator = require('file-stream-rotator');
var merge = require('merge');
var https = require('https');
var cbor = require('cbor');
var moment = require('moment');

var mqtt = require('mqtt');
global.noti_mqtt = null;

global.NOPRINT = 'true';
global.ONCE = 'true';

var cb = require('./mobius/cb');
var responder = require('./mobius/responder');
var resource = require('./mobius/resource');
var security = require('./mobius/security');
var fopt = require('./mobius/fopt');
var tr = require('./mobius/tr');
var sgn = require('./mobius/sgn');

var db = require('./mobius/db_action');
var db_sql = require('./mobius/sql_action');

// ������ �����մϴ�.
var app = express();

global.usespid              = '//keti.re.kr';
global.usesuperuser         = 'Superman';

global.useobserver          = 'Sandwich';

global.resultStatusCode = {
    '4230': "LOCKED: this resource was occupied by others",
    '4103': "ACCESS DENIED"
};

global.randomValueBase64 = function (len) {
    return crypto.randomBytes(Math.ceil(len * 3 / 4))
        .toString('base64')   // convert to base64 format
        .slice(0, len)        // return required number of characters
        .replace(/\+/g, '0')  // replace '+' with '0'
        .replace(/\//g, '0'); // replace '/' with '0'
};

global.randomIntInc = function (low, high) {
    return Math.floor(Math.random() * (high - low + 1) + low);
};

global.randomValue = function (qty) {
    return crypto.randomBytes(qty).toString(2);
};

var logDirectory = __dirname + '/log';

// ensure log directory exists
fs.existsSync(logDirectory) || fs.mkdirSync(logDirectory);

// create a rotating write stream
var accessLogStream = fileStreamRotator.getStream({
    date_format: 'YYYYMMDD',
    filename: logDirectory + '/access-%DATE%.log',
    frequency: 'daily',
    verbose: false
});

// setup the logger
app.use(morgan('combined', {stream: accessLogStream}));
//ts_app.use(morgan('short', {stream: accessLogStream}));

function del_req_resource() {
    db_sql.delete_req(function (err, delete_Obj) {
        if(!err) {
            console.log('deleted ' + delete_Obj.affectedRows + ' request resource(s).');
        }
    });
}

function del_expired_resource() {
    // this routine is that delete resource expired time exceed et of resource
    var et = moment().utc().format('YYYYMMDDTHHmmss');
    db_sql.delete_lookup_et(et, function (err) {
        if(!err) {
            console.log('---------------');
            console.log('delete resources expired et');
            console.log('---------------');
        }
    });
}

function noti_mqtt_begin() {
    if(noti_mqtt == null) {
        if(usesecure === 'disable') {
            noti_mqtt = mqtt.connect('mqtt://' + usemqttbroker + ':' + usemqttport);
        }
        else {
            var connectOptions = {
                host: usemqttbroker,
                port: usemqttport,
                protocol: "mqtts",
                keepalive: 15,
                //             clientId: serverUID,
                protocolId: "MQTT",
                protocolVersion: 4,
                clean: true,
                reconnectPeriod: 2000,
                connectTimeout: 2000,
                key: fs.readFileSync("./server-key.pem"),
                cert: fs.readFileSync("./server-crt.pem"),
                rejectUnauthorized: false
            };
            noti_mqtt = mqtt.connect(connectOptions);
        }

        noti_mqtt.on('connect', function () {
            console.log('noti_mqtt is connected');
        });

        noti_mqtt.on('message', sgn.response_noti_handler);
    }
}

var cluster = require('cluster');
var os = require('os');
var cpuCount = os.cpus().length;
var worker = [];
var ss_ri_cache = {};

global.get_ss_ri_cache = function (name) {
    return ss_ri_cache[name];
};

global.get_all_ss_ri_cache = function () {
    return ss_ri_cache;
};

global.set_ss_ri_cache = function (name, val) {
    ss_ri_cache[name] = val;

    if ( cluster.isWorker ) {
        process.send({
            cmd: 'ss_ri:edit-request',
            name: name,
            val: val
        });
    }
    else {
        broadcast_ss_ri_cache();
    }
};

global.del_ss_ri_cache = function (name) {
    delete ss_ri_cache[name];

    if ( cluster.isWorker ) {
        process.send({
            cmd: 'ss_ri:del-request',
            name: name
        });
    }
    else {
        broadcast_ss_ri_cache();
    }
};

function broadcast_ss_ri_cache() {
    if ( cluster.isMaster ) {
        for (var id in cluster.workers) {
            if(cluster.workers.hasOwnProperty(id)) {
                var worker = cluster.workers[id];

                worker.send({
                    cmd: 'ss_ri:edit',
                    data: ss_ri_cache
                });
            }
        }
    }
    else {
        process.send({
            cmd: 'ss_ri:broadcast'
        });
    }
}

global.cbs_cache = {};

global.set_cbs_cache = function (name, val) {
    cbs_cache[name] = val;

    if ( cluster.isWorker ) {
        process.send({
            cmd: 'cbs:broadcast_set',
            name: name,
            val: val
        });
    }
    else {
        broadcast_set_cbs_cache(name, val);
    }
};

function broadcast_set_cbs_cache(name, val) {
    if ( cluster.isMaster ) {
        for (var id in cluster.workers) {
            if(cluster.workers.hasOwnProperty(id)) {
                var worker = cluster.workers[id];

                worker.send({
                    cmd: 'cbs:edit_set',
                    name: name,
                    val: val
                });
            }
        }
    }
    else {
        process.send({
            cmd: 'cbs:broadcast_set'
        });
    }
}

global.del_cbs_cache = function (name) {
    delete cbs_cache[name];

    if ( cluster.isWorker ) {
        process.send({
            cmd: 'cbs:broadcast',
            name: name
        });
    }
    else {
        broadcast_cbs_cache();
    }
};

function broadcast_cbs_cache() {
    if ( cluster.isMaster ) {
        for (var id in cluster.workers) {
            if(cluster.workers.hasOwnProperty(id)) {
                var worker = cluster.workers[id];

                worker.send({
                    cmd: 'cbs:edit',
                    data: cbs_cache
                });
            }
        }
    }
    else {
        process.send({
            cmd: 'cbs:broadcast'
        });
    }
}

var hit_cache = {};

global.get_hit_cache = function (name) {
    return hit_cache[name];
};

global.get_all_hit_cache = function () {
    return hit_cache;
};

global.set_hit_cache = function (name, val) {
    hit_cache[name] = val;

    if ( cluster.isWorker ) {
        process.send({
            cmd: 'hit:broadcast_set',
            name: name,
            val: val
        });
    }
    else {
        broadcast_set_hit_cache(name, val);
    }
};

function broadcast_set_hit_cache(name, val) {
    if ( cluster.isMaster ) {
        for (var id in cluster.workers) {
            if(cluster.workers.hasOwnProperty(id)) {
                var worker = cluster.workers[id];

                worker.send({
                    cmd: 'hit:edit_set',
                    name: name,
                    val: val
                });
            }
        }
    }
    else {
        process.send({
            cmd: 'hit:broadcast_set'
        });
    }
}

function broadcast_hit_cache() {
    if ( cluster.isMaster ) {
        for (var id in cluster.workers) {
            if(cluster.workers.hasOwnProperty(id)) {
                var worker = cluster.workers[id];

                worker.send({
                    cmd: 'hit:edit',
                    data: hit_cache
                });
            }
        }
    }
    else {
        process.send({
            cmd: 'hit:broadcast'
        });
    }
}

var use_clustering = 1;
var worker_init_count = 0;
if (use_clustering) {
    if (cluster.isMaster) {
        console.log('CPU Count:', cpuCount);
        for (var i = 0; i < cpuCount; i++) {
            worker[i] = cluster.fork();
        }

        cluster.on('message', function(worker, message) {
            if(message.cmd === 'init:notify' ) {
                worker_init_count++;
                if(worker_init_count >= cpuCount) {
                    try {
                        var hitStr = fs.readFileSync('hit.json', 'utf8');
                        hit_cache = JSON.parse(hitStr);

                        var moment = require('moment');
                        var a = moment().utc();
                        var cur_t = a.format('YYYYMMDD');
                        if (!hit_cache.hasOwnProperty(cur_t)) {
                            hit_cache[cur_t] = [];
                            for (var h = 0; h < 24; h++) {
                                hit_cache[cur_t].push({});
                            }
                        }
                        broadcast_hit_cache();
                    }
                    catch (e) {
                        moment = require('moment');
                        a = moment().utc();
                        cur_t = a.format('YYYYMMDD');
                        if (!hit_cache.hasOwnProperty(cur_t)) {
                            hit_cache[cur_t] = [];
                            for (h = 0; h < 24; h++) {
                                hit_cache[cur_t].push({});
                            }
                        }
                        broadcast_hit_cache();
                    }

                    cbs_cache = {};
                    broadcast_cbs_cache();
                }
            }
            else if(message.cmd === 'ss_ri:edit-request' ) {
                ss_ri_cache[message.name] = message.val;
                broadcast_ss_ri_cache();
            }
            else if(message.cmd === 'ss_ri:del-request' ) {
                delete ss_ri_cache[message.name];
                broadcast_ss_ri_cache();
            }
            else if (message.cmd === 'ss_ri:broadcast') {
                broadcast_ss_ri_cache();
            }
            else if(message.cmd === 'cbs:broadcast_set' ) {
                cbs_cache[message.name] = message.val;
                console.log(message.name + ' : ' + JSON.stringify(cbs_cache[message.name]));
                broadcast_set_cbs_cache(message.name, message.val);
            }
            else if (message.cmd === 'cbs:broadcast') {
                delete cbs_cache[message.name];
                broadcast_cbs_cache();
            }
            else if (message.cmd === 'hit:broadcast_set') {
                hit_cache[message.name] = message.val;
                console.log(message.name + ' : ' + JSON.stringify(hit_cache[message.name]));
                fs.writeFileSync('hit.json', JSON.stringify(hit_cache, null, 4), 'utf8');
                broadcast_set_hit_cache(message.name, message.val);
            }
            else if (message.cmd === 'hit:broadcast') {
                delete hit_cache[message.name];
                broadcast_hit_cache();
            }
        });

        cluster.on('death', function (worker) {
            console.log('worker' + worker.pid + ' died --> start again');
            cluster.fork();
        });

        db.connect(usedbhost, 3306, 'root', usedbpass, function (rsc) {
            if (rsc == '1') {
                cb.create(function (rsp) {
                    console.log(JSON.stringify(rsp));

                    wdt.set_wdt(require('shortid').generate(), 43200, del_req_resource);
                    wdt.set_wdt(require('shortid').generate(), 86400, del_expired_resource);

                    require('./pxy_mqtt');
                    require('./pxy_coap');
                    require('./pxy_ws');
                    require('./cache_man');

                    if (usecsetype == 'mn' || usecsetype == 'asn') {
                        global.refreshIntervalId = setInterval(function () {
                            csr_custom.emit('register_remoteCSE');
                        }, 5000);
                    }
                });
            }
        });
    }
    else {
        //   app.use(bodyParser.urlencoded({ extended: true }));
        //   app.use(bodyParser.json({limit: '1mb', type: 'application/*+json' }));
        //   app.use(bodyParser.text({limit: '1mb', type: 'application/*+xml' }));

        process.send({
            cmd: 'init:notify'
        });

        process.on('message', function (message) {
            if (message.cmd === 'ss_ri:edit') {
                //ss_ri_cache[message.name] = message.val;
                ss_ri_cache = message.data;
                //console.log(ss_ri_cache);
            }
            else if (message.cmd === 'cbs:edit_set') {
                cbs_cache[message.name] = message.val;
                //console.log(message.val);
            }
            else if (message.cmd === 'cbs:edit') {
                //cbs_cache[message.name] = message.val;
                cbs_cache = message.data;
                //console.log(cbs_cache);
            }
            else if (message.cmd === 'hit:edit_set') {
                hit_cache[message.name] = message.val;
                //console.log(message.val);
            }
            else if (message.cmd === 'hit:edit') {
                hit_cache = message.data;
                //console.log(hit_cache);
            }
        });

        db.connect(usedbhost, 3306, 'root', usedbpass, function (rsc) {
            if (rsc == '1') {
                if(usesecure === 'disable') {
                    http.globalAgent.maxSockets = 1000000;
                    http.createServer(app).listen({port: usecsebaseport, agent: false}, function () {
                        console.log('mobius server (' + ip.address() + ') running at ' + usecsebaseport + ' port');
                        cb.create(function (rsp) {
                            console.log(JSON.stringify(rsp));
                            noti_mqtt_begin();
                        });
                    });
                }
                else {
                    var options = {
                        key: fs.readFileSync('server-key.pem'),
                        cert: fs.readFileSync('server-crt.pem'),
                        ca: fs.readFileSync('ca-crt.pem')
                    };
                    https.globalAgent.maxSockets = 1000000;
                    https.createServer(options, app).listen({port: usecsebaseport, agent: false}, function () {
                        console.log('mobius server (' + ip.address() + ') running at ' + usecsebaseport + ' port');
                        cb.create(function (rsp) {
                            console.log(JSON.stringify(rsp));
                            noti_mqtt_begin();
                        });
                    });
                }
            }
        });
    }
}
else {
    db.connect(usedbhost, 3306, 'root', usedbpass, function (rsc) {
        if (rsc == '1') {
            cb.create(function (rsp) {
                console.log(JSON.stringify(rsp));

                if(usesecure === 'disable') {
                    http.globalAgent.maxSockets = 1000000;
                    http.createServer(app).listen({port: usecsebaseport, agent: false}, function () {
                        console.log('mobius server (' + ip.address() + ') running at ' + usecsebaseport + ' port');
                        require('./pxy_mqtt');
                        noti_mqtt_begin();

                        if (usecsetype === 'mn' || usecsetype === 'asn') {
                            global.refreshIntervalId = setInterval(function () {
                                csr_custom.emit('register_remoteCSE');
                            }, 5000);
                        }
                    });
                }
                else {
                    var options = {
                        key: fs.readFileSync('server-key.pem'),
                        cert: fs.readFileSync('server-crt.pem'),
                        ca: fs.readFileSync('ca-crt.pem')
                    };
                    https.globalAgent.maxSockets = 1000000;
                    https.createServer(options, app).listen({port: usecsebaseport, agent: false}, function () {
                        console.log('mobius server (' + ip.address() + ') running at ' + usecsebaseport + ' port');
                        require('./pxy_mqtt');
                        noti_mqtt_begin();
                        //require('./mobius/ts_agent');

                        if (usecsetype === 'mn' || usecsetype === 'asn') {
                            global.refreshIntervalId = setInterval(function () {
                                csr_custom.emit('register_remoteCSE');
                            }, 5000);
                        }
                    });
                }
            });
        }
    });
}

global.get_ri_list_sri = function (request, response, sri_list, ri_list, count, callback) {
    if(sri_list.length <= count) {
        callback(sri_list, request, response);
    }
    else {
        db_sql.get_ri_sri(request, response, sri_list[count], function (err, results, request, response) {
            ri_list[count] = ((results.length == 0) ? sri_list[count] : results[0].ri);

            if (sri_list.length <= ++count) {
                callback(ri_list, request, response);
            }
            else {
                get_ri_list_sri(request, response, sri_list, ri_list, count, function (ri_list, request, response) {
                    callback(ri_list, request, response);
                });
            }
        });
    }
};

global.update_route = function (callback) {
    var cse_poa = {};
    db_sql.select_csr_like(usecsebase, function (err, results_csr) {
        if (!err) {
            for (var i = 0; i < results_csr.length; i++) {
                var poa_arr = JSON.parse(results_csr[i].poa);
                for (var j = 0; j < poa_arr.length; j++) {
                    if (url.parse(poa_arr[j]).protocol == 'http:' || url.parse(poa_arr[j]).protocol == 'https:') {
                        cse_poa[results_csr[i].ri.split('/')[2]] = poa_arr[j];
                    }
                }
            }
        }
        callback(cse_poa);
    });
};

function make_short_nametype(body_Obj) {
    if (body_Obj[Object.keys(body_Obj)[0]]['$'] != null) {
        if (body_Obj[Object.keys(body_Obj)[0]]['$'].rn != null) {
            body_Obj[Object.keys(body_Obj)[0]].rn = body_Obj[Object.keys(body_Obj)[0]]['$'].rn;
        }
        delete body_Obj[Object.keys(body_Obj)[0]]['$'];
    }

    var rootnm = Object.keys(body_Obj)[0].split(':')[1];
    body_Obj[rootnm] = body_Obj[Object.keys(body_Obj)[0]];
    delete body_Obj[Object.keys(body_Obj)[0]];

    for (var attr in body_Obj[rootnm]) {
        if (body_Obj[rootnm].hasOwnProperty(attr)) {
            if (typeof body_Obj[rootnm][attr] === 'boolean') {
                body_Obj[rootnm][attr] = body_Obj[rootnm][attr].toString();
            }
            else if (typeof body_Obj[rootnm][attr] === 'string') {
            }
            else if (typeof body_Obj[rootnm][attr] === 'number') {
                body_Obj[rootnm][attr] = body_Obj[rootnm][attr].toString();
            }
            else {
            }
        }
    }
}

global.make_json_obj = function(bodytype, str, callback) {
    try {
        if (bodytype === 'xml') {
            var message = str;
            var parser = new xml2js.Parser({explicitArray: false});
            parser.parseString(message.toString(), function (err, result) {
                if (err) {
                    console.log('[mqtt make json obj] xml2js parser error]');
                    callback('0');
                }
                else {
                    for (var prop in result) {
                        if (result.hasOwnProperty(prop)) {
                            for (var attr in result[prop]) {
                                if (result[prop].hasOwnProperty(attr)) {
                                    if (attr == '$') {
                                        delete result[prop][attr];
                                    }
                                    else if (attr == 'pc') {
                                        make_json_arraytype(result[prop][attr]);
                                    }
                                }
                            }
                        }
                    }
                    callback('1', result);
                }
            });
        }
        else if (bodytype === 'cbor') {
            cbor.decodeFirst(str, function(err, result) {
                if (err) {
                    console.log('cbor parser error]');
                }
                else {
                    callback('1', result);
                }
            });
        }
        else {
            var result = JSON.parse(str);
            callback('1', result);
        }
    }
    catch (e) {
        console.error(e.message);
        callback('0');
    }
};

global.make_json_arraytype = function (body_Obj) {
    for (var prop in body_Obj) {
        if (body_Obj.hasOwnProperty(prop)) {
            for (var attr in body_Obj[prop]) {
                if (body_Obj[prop].hasOwnProperty(attr)) {
                    if (attr == 'srv' || attr == 'aa' || attr == 'at' || attr == 'poa' || attr == 'lbl' || attr == 'acpi' || attr == 'srt' || attr == 'nu' || attr == 'mid' || attr == 'macp' || attr == 'rels') {
                        if (body_Obj[prop][attr]) {
                            body_Obj[prop][attr] = body_Obj[prop][attr].split(' ');
                        }
                        if (body_Obj[prop][attr] == '') {
                            body_Obj[prop][attr] = [];
                        }
                        if (body_Obj[prop][attr] == '[]') {
                            body_Obj[prop][attr] = [];
                        }
                    }

                    if (attr == 'rqps') {
                        var rqps_type = getType(body_Obj[prop][attr]);
                        if (rqps_type === 'array') {

                        }
                        else if (rqps_type === 'object') {
                            var temp = body_Obj[prop][attr];
                            body_Obj[prop][attr] = [];
                            body_Obj[prop][attr].push(temp);
                        }
                        else {

                        }
                    }

                    if (attr == 'enc') {
                        if (body_Obj[prop][attr]) {
                            if (body_Obj[prop][attr].net) {
                                body_Obj[prop][attr].net = body_Obj[prop][attr].net.split(' ');
                            }
                        }
                    }

                    if (attr == 'pv' || attr == 'pvs') {
                        if (body_Obj[prop][attr]) {
                            if (body_Obj[prop][attr].acr) {
                                if (!Array.isArray(body_Obj[prop][attr].acr)) {
                                    temp = body_Obj[prop][attr].acr;
                                    body_Obj[prop][attr].acr = [];
                                    body_Obj[prop][attr].acr[0] = temp;
                                }

                                for (var acr_idx in body_Obj[prop][attr].acr) {
                                    if (body_Obj[prop][attr].acr.hasOwnProperty(acr_idx)) {
                                        if (body_Obj[prop][attr].acr[acr_idx].acor) {
                                            body_Obj[prop][attr].acr[acr_idx].acor = body_Obj[prop][attr].acr[acr_idx].acor.split(' ');
                                        }

                                        if (body_Obj[prop][attr].acr[acr_idx].hasOwnProperty('acco')) {
                                            if (!Array.isArray(body_Obj[prop][attr].acr[acr_idx].acco)) {
                                                temp = body_Obj[prop][attr].acr[acr_idx].acco;
                                                body_Obj[prop][attr].acr[acr_idx].acco = [];
                                                body_Obj[prop][attr].acr[acr_idx].acco[0] = temp;
                                            }

                                            var acco = body_Obj[prop][attr].acr[acr_idx].acco;
                                            for(var acco_idx in acco) {
                                                if(acco.hasOwnProperty(acco_idx)) {
                                                    if (acco[acco_idx].hasOwnProperty('acip')) {
                                                        if (acco[acco_idx].acip.hasOwnProperty('ipv4')) {
                                                            if (getType(acco[acco_idx].acip['ipv4']) == 'string') {
                                                                acco[acco_idx].acip['ipv4'] = acco[acco_idx].acip.ipv4.split(' ');
                                                            }
                                                        }
                                                        else if (acco[acco_idx].acip.hasOwnProperty('ipv6')) {
                                                            if (getType(acco[acco_idx].acip['ipv6']) == 'string') {
                                                                acco[acco_idx].acip['ipv6'] = acco[acco_idx].acip.ipv6.split(' ');
                                                            }
                                                        }
                                                    }
                                                    if (acco[acco_idx].hasOwnProperty('actw')) {
                                                        if (getType(acco[acco_idx].actw) == 'string') {
                                                            temp = acco[acco_idx].actw;
                                                            acco[acco_idx]['actw'] = [];
                                                            acco[acco_idx].actw[0] = temp;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (body_Obj[prop][attr].acr == '') {
                                body_Obj[prop][attr].acr = [];
                            }

                            if (body_Obj[prop][attr].acr == '[]') {
                                body_Obj[prop][attr].acr = [];
                            }
                        }
                    }
                }
            }
        }
    }
};

function check_body_format(request) {
    request.headers.usebodytype = 'json';
    if (request.headers.accept) {
        try {
            if ((request.headers.accept.split('/')[1] == 'xml') || (request.headers.accept.split('+')[1] == 'xml')) {
                request.headers.usebodytype = 'xml';
            }
            else if ((request.headers.accept.split('/')[1] == 'cbor') || (request.headers.accept.split('+')[1] == 'cbor')) {
                request.headers.usebodytype = 'cbor';
            }
        }
        catch (e) {
        }
    }
}

function check_http_body(request, response, callback) {
    var body_Obj = {};

    if (request.body == "") {
        responder.error_result(request, response, 400, 4000, 'body is empty');
        callback('0', body_Obj, 'json', request, response);
        return '0';
    }

    //console.log(request.body);

    try {
        var content_type = request.headers['content-type'].split(';');
    }
    catch (e) {
        responder.error_result(request, response, 400, 4000, 'content-type is none');
        callback('0', body_Obj, content_type, request, response);
        return '0';
    }

    if(request.headers['content-type'].includes('xml')) {
        request.headers.usebodytype = 'xml';
    }
    else if(request.headers['content-type'].includes('cbor')) {
        request.headers.usebodytype = 'cbor';
    }
    else {
        request.headers.usebodytype = 'json';
    }

    if (request.headers.usebodytype === 'xml') {
        try {
            var parser = new xml2js.Parser({explicitArray: false});
            parser.parseString(request.body.toString(), function (err, result) {
                if (err) {
                    responder.error_result(request, response, 400, 4000, 'do not parse xml body' + err.message);
                    callback('0', body_Obj, content_type, request, response);
                }
                else {
                    body_Obj = result;
                    make_short_nametype(body_Obj);
                    make_json_arraytype(body_Obj);

                    callback('1', body_Obj, content_type, request, response);
                }
            });
        }
        catch(e) {
            responder.error_result(request, response, 400, 4000, 'do not parse xml body');
            callback('0', body_Obj, content_type, request, response);
        }
    }
    else if (request.headers.usebodytype === 'cbor') {
        try {
            var encoded = request.body;
            cbor.decodeFirst(encoded, function(err, body_Obj) {
                if (err) {
                    responder.error_result(request, response, 400, 4000, 'do not parse cbor body');
                    callback('0', body_Obj, content_type, request, response);
                }
                else {
                    make_short_nametype(body_Obj);
                    //make_json_arraytype(body_Obj);

                    callback('1', body_Obj, content_type, request, response);
                }
            });
        }
        catch(e) {
            responder.error_result(request, response, 400, 4000, 'do not parse cbor body');
            callback('0', body_Obj, content_type, request, response);
        }
    }
    else {
        try {
            body_Obj = JSON.parse(request.body.toString());
            make_short_nametype(body_Obj);

            if (Object.keys(body_Obj)[0] == 'undefined') {
                responder.error_result(request, response, 400, 4000, 'root tag of body is not matched');
                callback('0', body_Obj, content_type, request, response);
                return 0;
            }
            callback('1', body_Obj, content_type, request, response);
        }
        catch (e) {
            responder.error_result(request, response, 400, 4000, 'do not parse json body');
            callback('0', body_Obj, content_type, request, response);
        }
    }
}

function check_http(request, response, callback) {
    request.headers.rootnm = 'dbg';

    var body_Obj = {};

    if (request.query.real == 4) {
        if (request.method == 'POST' || request.method == 'PUT') {
            try {
                var content_type = request.headers['content-type'].split(';');
                var ty = '99';
                for(var i in content_type) {
                    if(content_type.hasOwnProperty(i)) {
                        var ty_arr = content_type[i].replace(/ /g, '').split('=');
                        if(ty_arr[0].replace(/ /g, '') == 'ty') {
                            ty = ty_arr[1].replace(' ', '');
                            break;
                        }
                    }
                }

                if(ty == '99') {
                    responder.error_result(request, response, 400, 4000, 'ty is none');
                    callback('0', body_Obj, request, response);
                    return '0';
                }

                body_Obj = JSON.parse(request.body.toString());
                var rootnm = Object.keys(body_Obj)[0].split(':')[1];
                body_Obj[rootnm] = body_Obj[Object.keys(body_Obj)[0]];
                delete body_Obj[Object.keys(body_Obj)[0]];
                request.headers.rootnm = rootnm;
                callback(ty, body_Obj, request, response);
                return '0';
            }
            catch (e) {
                responder.error_result(request, response, 400, 4000, 'BAD REQUEST: can not apply real query');
                callback('0', body_Obj, request, response);
                return '0';
            }
        }
        else {
            callback('direct', body_Obj, request, response);
            return '0';
        }
    }

    // Check X-M2M-RI Header
    if ((request.headers['x-m2m-ri'] == null)) {
        responder.error_result(request, response, 400, 4000, 'BAD REQUEST: X-M2M-RI is none');
        callback('0', body_Obj, request, response);
        return '0';
    }

    // Check X-M2M-RVI Header
    if ((request.headers['x-m2m-rvi'] == null)) {
        // responder.error_result(request, response, 400, 4000, 'BAD REQUEST: X-M2M-RI is none');
        // callback('0', body_Obj, request, response);
        // return '0';
        // todo: RVI check
        request.headers['x-m2m-rvi'] = uservi;
    }


    // Check X-M2M-Origin Header

    if(request.headers['x-m2m-origin'] == null) {
        if (request.method != 'POST') {
            responder.error_result(request, response, 400, 4000, 'BAD REQUEST: X-M2M-Origin Header is Mandatory');
            callback('0', body_Obj, request, response);
            return '0';
        }
    }
    else {
        var allow = 1;
        if(allowed_ae_ids.length > 0) {
            allow = 0;
            for(var idx in allowed_ae_ids) {
                if(allowed_ae_ids.hasOwnProperty(idx)) {
                    if(usecseid == request.headers['x-m2m-origin']) {
                        allow = 1;
                        break;
                    }
                    else if(allowed_ae_ids[idx] == request.headers['x-m2m-origin']) {
                        allow = 1;
                        break;
                    }
                }
            }

            if(allow == 0) {
                responder.error_result(request, response, 403, 4107, 'OPERATION_NOT_ALLOWED: AE-ID is not allowed');
                callback('0', body_Obj, request, response);
                return '0';
            }
        }
    }

    var url_arr = url.parse(request.url).pathname.split('/');
    var last_url = url_arr[url_arr.length - 1];

    if (request.method == 'POST' || request.method == 'PUT') {
        check_http_body(request, response, function (rsc, body_Obj, content_type, request, response) {
            if (rsc == '1') {
                if (request.method == 'POST') {
                    try {
                        var ty = '99';
                        for(var i in content_type) {
                            if(content_type.hasOwnProperty(i)) {
                                var ty_arr = content_type[i].replace(/ /g, '').split('=');
                                if(ty_arr[0].replace(/ /g, '') == 'ty') {
                                    ty = ty_arr[1].replace(' ', '');
                                    break;
                                }
                            }
                        }

                        if(ty == '99') {
                            responder.error_result(request, response, 400, 4000, 'ty is none');
                            callback('0', body_Obj, request, response);
                            return '0';
                        }

                        if(request.headers['x-m2m-origin'] == null) {
                            if (ty == '2' || ty == '16') {
                                request.headers['x-m2m-origin'] = 'S';
                            }
                            else {
                                responder.error_result(request, response, 400, 4000, 'BAD REQUEST: X-M2M-Origin Header is Mandatory');
                                callback('0', body_Obj, request, response);
                                return '0';
                            }
                        }

                        if(request.headers['x-m2m-origin'] == '') {
                            responder.error_result(request, response, 400, 4000, 'BAD REQUEST: X-M2M-Origin Header is Mandatory');
                            callback('0', body_Obj, request, response);
                            return '0';
                        }

                        if(ty == '2') {
                            var allow = 1;
                            if(allowed_app_ids.length > 0) {
                                allow = 0;
                                for(var idx in allowed_app_ids) {
                                    if(allowed_app_ids.hasOwnProperty(idx)) {
                                        if(allowed_app_ids[idx] == body_Obj.ae.api) {
                                            allow = 1;
                                            break;
                                        }
                                    }
                                }

                                if(allow == 0) {
                                    responder.error_result(request, response, 403, 4107, 'OPERATION_NOT_ALLOWED: APP-ID in AE is not allowed');
                                    callback('0', body_Obj, request, response);
                                    return '0';
                                }
                            }
                        }
                    }
                    catch (e) {
                        responder.error_result(request, response, 400, 4000, 'ty is none');
                        callback('0', body_Obj, request, response);
                        return '0';
                    }

                    if(responder.typeRsrc[ty] == null) {
                        responder.error_result(request, response, 405, 4005, 'OPERATION_NOT_ALLOWED: we do not support ' + Object.keys(body_Obj)[0] + '(' + ty + ') resource');
                        callback('0', body_Obj, request, response);
                        return '0';
                    }

                    /* ignore from (origin)
                    if(request.headers['x-m2m-origin'].charAt(0) == '/') {
                        if(request.headers['x-m2m-origin'].split('/').length > 2) {
                            if((request.headers['x-m2m-origin'].split('/')[3].charAt(0) == 'S' || request.headers['x-m2m-origin'].split('/')[3].charAt(0) == 'C')) {  // origin is SP-relative-ID
                            }
                            else {
                                console.log(request.headers['x-m2m-origin']);
                                body_Obj = {};
                                body_Obj['dbg'] = 'BAD REQUEST: When request to create AE, AE-ID should start \'S\' or \'C\' of AE-ID in X-M2M-Origin Header';
                                responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                callback('0', body_Obj, request, response);
                                return '0';
                            }
                        }
                        else {
                        }
                    }
                    else if((request.headers['x-m2m-origin'].charAt(0) == 'S' || request.headers['x-m2m-origin'].charAt(0) == 'C')) {
                    }
                    else {
                        console.log(request.headers['x-m2m-origin']);
                        body_Obj = {};
                        body_Obj['dbg'] = 'BAD REQUEST: When request to create AE, AE-ID should start \'S\' or \'C\' of AE-ID in X-M2M-Origin Header';
                        responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                        callback('0', body_Obj, request, response);
                        return '0';
                    }
                    */

                    if (ty == '5') {
                        responder.error_result(request, response, 405, 4005, 'OPERATION_NOT_ALLOWED: CSEBase can not be created by others');
                        callback('0', body_Obj, request, response);
                        return '0';
                    }

                    if (ty == '17') {
                        responder.error_result(request, response, 405, 4005, 'OPERATION_NOT_ALLOWED (req is not supported when post request)');
                        callback('0', body_Obj, request, response);
                        return '0';
                    }

                    if (responder.typeRsrc[ty] != Object.keys(body_Obj)[0]) {
                        if(responder.typeRsrc[ty] == 'mgo') {
                            var support_mgo = 0;
                            for (var prop in responder.mgoType) {
                                if(responder.mgoType.hasOwnProperty(prop)) {
                                    if (responder.mgoType[prop] == Object.keys(body_Obj)[0]) {
                                        support_mgo = 1;
                                        break;
                                    }
                                }
                            }

                            if(support_mgo == 0) {
                                responder.error_result(request, response, 400, 4000, 'ty [' + ty + '] is different with body (' + Object.keys(body_Obj)[0] + ')');
                                callback('0', body_Obj, request, response);
                                return '0';
                            }
                        }
                        else {
                            responder.error_result(request, response, 400, 4000, 'ty [' + ty + '] is different with body (' + Object.keys(body_Obj)[0] + ')');
                            callback('0', body_Obj, request, response);
                            return '0';
                        }
                    }
                }
                else { // PUT
                    ty = '99';
                    for (var ty_idx in responder.typeRsrc) {
                        if (responder.typeRsrc.hasOwnProperty(ty_idx)) {
                            if ((ty_idx == 4) && (responder.typeRsrc[ty_idx] == Object.keys(body_Obj)[0])) {
                                responder.error_result(request, response, 405, 4005, 'Update cin is not supported');
                                callback('0', body_Obj, request, response);
                                return '0';
                            }
                            else if ((ty_idx != 4) && (responder.typeRsrc[ty_idx] == Object.keys(body_Obj)[0])) {
                                if ((ty_idx == 17) && (responder.typeRsrc[ty_idx] == Object.keys(body_Obj)[0])) {
                                    responder.error_result(request, response, 405, 4005, 'OPERATION_NOT_ALLOWED (req is not supported when put request)');
                                    callback('0', body_Obj, request, response);
                                    return 0;
                                }
                                else {
                                    ty = ty_idx;
                                    break;
                                }
                            }
                            else if (ty_idx == 13) {
                                for (var mgo_idx in responder.mgoType) {
                                    if (responder.mgoType.hasOwnProperty(mgo_idx)) {
                                        if ((responder.mgoType[mgo_idx] == Object.keys(body_Obj)[0])) {
                                            ty = ty_idx;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (ty == '99') {
                        responder.error_result(request, response, 400, 4000, 'resource type in body is not match to target resource');
                        callback('0', body_Obj, request, response);
                        return '0';
                    }
                }

                request.headers.rootnm = Object.keys(body_Obj)[0];

                for (prop in body_Obj) {
                    if (body_Obj.hasOwnProperty(prop)) {
                        for (var attr in body_Obj[prop]) {
                            if (body_Obj[prop].hasOwnProperty(attr)) {
                                if(attr == 'aa' || attr == 'at' || attr == 'poa' || attr == 'acpi' || attr == 'srt' ||
                                    attr == 'nu' || attr == 'mid' || attr == 'macp' || attr == 'rels' || attr == 'rqps' || attr == 'srv') {
                                    if (!Array.isArray(body_Obj[prop][attr])) {
                                        body_Obj = {};
                                        body_Obj['dbg'] = attr + ' attribute should be json array format';
                                        responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                        callback('0', body_Obj, request, response);
                                        return '0';
                                    }
                                }
                                else if(attr == 'lbl') {
                                    if(body_Obj[prop][attr] == null) {
                                        body_Obj[prop][attr] = [];
                                    }
                                    else if (!Array.isArray(body_Obj[prop][attr])) {
                                        body_Obj = {};
                                        body_Obj['dbg'] = attr + ' attribute should be json array format';
                                        responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                        callback('0', body_Obj, request, response);
                                        return '0';
                                    }
                                }
                                else if (attr == 'enc') {
                                    if (body_Obj[prop][attr].net) {
                                        if (!Array.isArray(body_Obj[prop][attr].net)) {
                                            body_Obj = {};
                                            body_Obj['dbg'] = attr + '.net attribute should be json array format';
                                            responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                            callback('0', body_Obj, request, response);
                                            return '0';
                                        }
                                    }
                                    else {
                                        body_Obj = {};
                                        body_Obj['dbg'] = attr + 'attribute should have net key as child in json format';
                                        responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                        callback('0', body_Obj, request, response);
                                        return '0';
                                    }
                                }
                                else if (attr == 'pv' || attr == 'pvs') {
                                    if(body_Obj[prop][attr].hasOwnProperty('acr')) {
                                        if (!Array.isArray(body_Obj[prop][attr].acr)) {
                                            body_Obj = {};
                                            body_Obj['dbg'] = attr + '.acr should be json array format';
                                            responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                            callback('0', body_Obj, request, response);
                                            return '0';
                                        }

                                        var acr = body_Obj[prop][attr].acr;
                                        for (var acr_idx in acr) {
                                            if (acr.hasOwnProperty(acr_idx)) {
                                                if (acr[acr_idx].acor) {
                                                    if (!Array.isArray(acr[acr_idx].acor)) {
                                                        body_Obj = {};
                                                        body_Obj['dbg'] = attr + '.acr[' + acr_idx + '].acor should be json array format';
                                                        responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                                        callback('0', body_Obj, request, response);
                                                        return '0';
                                                    }
                                                }

                                                if (acr[acr_idx].acco) {
                                                    if (!Array.isArray(acr[acr_idx].acco)) {
                                                        body_Obj = {};
                                                        body_Obj['dbg'] = attr + '.acr[' + acr_idx + '].acco should be json array format';
                                                        responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                                        callback('0', body_Obj, request, response);
                                                        return '0';
                                                    }
                                                    for (var acco_idx in acr[acr_idx].acco) {
                                                        if (acr[acr_idx].acco.hasOwnProperty(acco_idx)) {
                                                            var acco = acr[acr_idx].acco[acco_idx];
                                                            if (acco.acip) {
                                                                if (acco.acip['ipv4']) {
                                                                    if (!Array.isArray(acco.acip['ipv4'])) {
                                                                        body_Obj = {};
                                                                        body_Obj['dbg'] = attr + '.acr[' + acr_idx + '].acco.acip.ipv4 should be json array format';
                                                                        responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                                                        callback('0', body_Obj, request, response);
                                                                        return '0';
                                                                    }
                                                                }
                                                                else if (acco.acip['ipv6']) {
                                                                    if (!Array.isArray(acco.acip['ipv6'])) {
                                                                        body_Obj = {};
                                                                        body_Obj['dbg'] = attr + '.acr[' + acr_idx + '].acco.acip.ipv6 should be json array format';
                                                                        responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                                                        callback('0', body_Obj, request, response);
                                                                        return '0';
                                                                    }
                                                                }
                                                            }

                                                            if (acco.actw) {
                                                                if (!Array.isArray(acco.actw)) {
                                                                    body_Obj = {};
                                                                    body_Obj['dbg'] = attr + '.acr[' + acr_idx + '].acco[' + acco_idx + '].actw should be json array format';
                                                                    responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                                                    callback('0', body_Obj, request, response);
                                                                    return '0';
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                else if (attr == 'uds') {
                                    if (body_Obj[prop][attr].can && body_Obj[prop][attr].sus) {
                                    }
                                    else {
                                        body_Obj = {};
                                        body_Obj['dbg'] = attr + ' attribute should have can and sus key in json format';
                                        responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                        callback('0', body_Obj, request, response);
                                        return '0';
                                    }
                                }
                                else if (attr == 'cas') {
                                    if (body_Obj[prop][attr].can && body_Obj[prop][attr].sus) {
                                    }
                                    else {
                                        body_Obj = {};
                                        body_Obj['dbg'] = attr + ' attribute should have can and sus key in json format';
                                        responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                                        callback('0', body_Obj, request, response);
                                        return '0';
                                    }
                                }
                                else {
                                }
                            }
                        }
                    }
                }

                callback(ty, body_Obj, request, response);
            }
        });
    }
    else if (request.method == 'GET' || request.method == 'DELETE') {
        if (last_url == 'latest' || last_url == 'la') {
            callback('latest', body_Obj, request, response);
        }
        else if (last_url == 'oldest' || last_url == 'ol') {
            callback('oldest', body_Obj, request, response);
        }
        else {
            callback('direct', body_Obj, request, response);
        }
    }
    else {
        body_Obj = {};
        body_Obj['dbg'] = 'request method is not supported';
        responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
        callback('0', body_Obj, request, response);
        return '0';
    }
}

function check_resource(request, response, body_Obj, callback) {
    var ri = url.parse(request.url).pathname;

    var chk_fopt = ri.split('/fopt');
    if(chk_fopt.length == 2) {
        //if (chk_fopt[1] == '') {
            //var url_arr = ri.split('/');
            //var last_url = url_arr[url_arr.length - 1];
            //var op = 'direct';
            //if (last_url == 'fanoutpoint' || last_url == 'fopt') {
                //ri = ri.replace('/fanoutpoint', '');
                //ri = ri.replace('/fopt', '');
                ri = chk_fopt[0];
                op = 'fanoutpoint';
                db_sql.select_grp_lookup(ri, function (err, result_Obj) {
                    if (!err) {
                        if (result_Obj.length == 1) {
                            result_Obj[0].acpi = JSON.parse(result_Obj[0].acpi);
                            result_Obj[0].lbl = JSON.parse(result_Obj[0].lbl);
                            result_Obj[0].aa = JSON.parse(result_Obj[0].aa);
                            result_Obj[0].at = JSON.parse(result_Obj[0].at);
                            callback('1', result_Obj[0], op, request, response, body_Obj);
                        }
                        else {
                            result_Obj = {};
                            result_Obj['dbg'] = 'resource does not exist';
                            responder.response_result(request, response, 404, result_Obj, 4004, request.url, result_Obj['dbg']);
                            callback('0', {}, '', request, response, body_Obj);
                            return '0';
                        }
                    }
                    else {
                        var code = result_Obj.message;
                        result_Obj = {};
                        result_Obj['dbg'] = code;
                        responder.response_result(request, response, 500, result_Obj, 5000, request.url, result_Obj['dbg']);
                        callback('0', {}, '', request, response, body_Obj);
                        return '0';
                    }
                });
            //}
        ////}
        ////else {
       //     chk_fopt[0]
     //   }
    }
    else {
        console.log('X-M2M-Origin: ' + request.headers['x-m2m-origin']);
        console.log(body_Obj);

        var url_arr = ri.split('/');
        var last_url = url_arr[url_arr.length - 1];
        var op = 'direct';
        var resource_Obj = {};

        if (last_url == 'latest' || last_url == 'la') {
            ri = ri.replace('/latest', '');
            ri = ri.replace('/la', '');
            op = 'latest';
            db_sql.select_direct_lookup(ri, function (err, parent_Comm) {
                if (!err) {
                    if (parent_Comm.length == 1) {
                        if (parent_Comm[0].ty == '3') {
                            var cur_ty = '4';
                        }
                        else if (parent_Comm[0].ty == '29') {
                            cur_ty = '30';
                        }
                        else {
                            parent_Comm = {};
                            parent_Comm['dbg'] = 'this resource can not have latest resource';
                            responder.response_result(request, response, 404, parent_Comm, 4004, request.url, parent_Comm['dbg']);
                            callback('0', {}, '', request, response, body_Obj);
                            return '0';
                        }
                        db_sql.select_resource(responder.typeRsrc[parent_Comm[0].ty], parent_Comm[0].ri, function (err, parent_Spec) {
                            if (!err) {
                                if (parent_Spec.length == 1) {
                                    var cur_d = new Date();
                                    var cur_cni = parent_Spec[0].cni;
                                    //var cur_lim = (request.query.hasOwnProperty('lim')) ? request.query.lim : '1';
                                    var cur_lim = '1';
                                    check_cbs_cache(ri, function (cni, cbs, st) {
                                        if(cni > 0) {
                                            db_sql.select_latest_resource(ri, cur_ty, 1, st, function (err, result_Obj) {
                                                if (!err) {
                                                    if (result_Obj.length == 1) {
                                                        makeObject(result_Obj[0]);
                                                        callback('1', result_Obj[0], op, request, response, body_Obj);
                                                    }
                                                    else {
                                                        result_Obj = {};
                                                        result_Obj['dbg'] = 'resource does not exist';
                                                        responder.response_result(request, response, 404, result_Obj, 4004, request.url, result_Obj['dbg']);
                                                        callback('0', {}, '', request, response, body_Obj);
                                                        return '0';
                                                    }
                                                }
                                                else {
                                                    var code = result_Obj.message;
                                                    result_Obj = {};
                                                    result_Obj['dbg'] = code;
                                                    responder.response_result(request, response, 500, result_Obj, 5000, request.url, result_Obj['dbg']);
                                                    callback('0', {}, '', request, response, body_Obj);
                                                    return '0';
                                                }
                                            });
                                        }
                                        else {
                                            result_Obj = {};
                                            result_Obj['dbg'] = 'resource does not exist';
                                            responder.response_result(request, response, 404, result_Obj, 4004, request.url, result_Obj['dbg']);
                                            callback('0', {}, '', request, response, body_Obj);
                                            return '0';
                                        }
                                    });
                                }
                                else {
                                    parent_Comm = {};
                                    parent_Comm['dbg'] = 'resource does not exist';
                                    responder.response_result(request, response, 404, parent_Comm, 4004, request.url, parent_Comm['dbg']);
                                    callback('0', {}, '', request, response, body_Obj);
                                    return '0';
                                }
                            }
                        });
                    }
                    else {
                        parent_Comm = {};
                        parent_Comm['dbg'] = 'resource does not exist';
                        responder.response_result(request, response, 404, parent_Comm, 4004, request.url, parent_Comm['dbg']);
                        callback('0', {}, '', request, response, body_Obj);
                        return '0';
                    }
                }
                else {
                    var code = parent_Comm.message;
                    parent_Comm = {};
                    parent_Comm['dbg'] = code;
                    responder.response_result(request, response, 500, parent_Comm, 5000, request.url, parent_Comm['dbg']);
                    callback('0', {}, '', request, response, body_Obj);
                    return '0';
                }
            });
        }
        else if (last_url == 'oldest' || last_url == 'ol') {
            ri = ri.replace('/oldest', '');
            ri = ri.replace('/ol', '');
            op = 'oldest';
            check_cbs_cache(ri, function (cni, cbs, st) {
                if(cni > 0) {
                    db_sql.select_oldest_resource(ri, function (err, result_Obj) {
                        if (!err) {
                            if (result_Obj.length == 1) {
                                makeObject(result_Obj[0]);
                                callback('1', result_Obj[0], op, request, response, body_Obj);
                            }
                            else {
                                result_Obj = {};
                                result_Obj['dbg'] = 'resource does not exist';
                                responder.response_result(request, response, 404, result_Obj, 4004, request.url, result_Obj['dbg']);
                                callback('0', {}, '', request, response, body_Obj);
                                return '0';
                            }
                        }
                        else {
                            var code = result_Obj.message;
                            result_Obj = {};
                            result_Obj['dbg'] = code;
                            responder.response_result(request, response, 500, result_Obj, 5000, request.url, result_Obj['dbg']);
                            callback('0', {}, '', request, response, body_Obj);
                            return '0';
                        }
                    });
                }
                else {
                    result_Obj = {};
                    result_Obj['dbg'] = 'resource does not exist';
                    responder.response_result(request, response, 404, result_Obj, 4004, request.url, result_Obj['dbg']);
                    callback('0', {}, '', request, response, body_Obj);
                    return '0';
                }
            });
        }
        else {
            op = 'direct';
            db_sql.select_direct_lookup(ri, function (err, comm_Obj) {
                if (!err) {
                    if (comm_Obj.length == 1) {
                        var ty = comm_Obj[0].ty;
                        if(ty == 3) {
                            check_cbs_cache(ri, function (cni, cbs, st) {
                                db_sql.select_resource(responder.typeRsrc[ty], comm_Obj[0].ri, function (err, spec_Obj) {
                                    if (!err) {
                                        if (spec_Obj.length == 1) {
                                            resource_Obj[responder.typeRsrc[ty]] = merge(comm_Obj[0], spec_Obj[0]);
                                            makeObject(resource_Obj[responder.typeRsrc[ty]]);
                                            callback('1', resource_Obj[responder.typeRsrc[ty]], op, request, response, body_Obj);
                                            return '0';
                                        }
                                        else {
                                            spec_Obj = {};
                                            spec_Obj['dbg'] = 'resource does not exist';
                                            responder.response_result(request, response, 404, spec_Obj, 4004, request.url, spec_Obj['dbg']);
                                            callback('0', resource_Obj);
                                            return '0';
                                        }
                                    }
                                    else {
                                        spec_Obj = {};
                                        spec_Obj['dbg'] = spec_Obj.message;
                                        responder.response_result(request, response, 500, spec_Obj, 5000, request.url, spec_Obj['dbg']);
                                        callback('0', resource_Obj);
                                        return '0';
                                    }
                                });
                            });
                        }
                        else {
                            db_sql.select_resource(responder.typeRsrc[ty], comm_Obj[0].ri, function (err, spec_Obj) {
                                if (!err) {
                                    if (spec_Obj.length == 1) {
                                        resource_Obj[responder.typeRsrc[ty]] = merge(comm_Obj[0], spec_Obj[0]);
                                        makeObject(resource_Obj[responder.typeRsrc[ty]]);
                                        callback('1', resource_Obj[responder.typeRsrc[ty]], op, request, response, body_Obj);
                                        return '0';
                                    }
                                    else {
                                        spec_Obj = {};
                                        spec_Obj['dbg'] = 'resource does not exist';
                                        responder.response_result(request, response, 404, spec_Obj, 4004, request.url, spec_Obj['dbg']);
                                        callback('0', resource_Obj);
                                        return '0';
                                    }
                                }
                                else {
                                    spec_Obj = {};
                                    spec_Obj['dbg'] = spec_Obj.message;
                                    responder.response_result(request, response, 500, spec_Obj, 5000, request.url, spec_Obj['dbg']);
                                    callback('0', resource_Obj);
                                    return '0';
                                }
                            });
                        }
                    }
                    else {
                        comm_Obj = {};
                        comm_Obj['dbg'] = 'resource does not exist';
                        responder.response_result(request, response, 404, comm_Obj, 4004, request.url, comm_Obj['dbg']);
                        callback('0', {}, '', request, response, body_Obj);
                        return '0';
                    }
                }
                else {
                    comm_Obj = {};
                    comm_Obj['dbg'] = comm_Obj.message;
                    responder.response_result(request, response, 500, comm_Obj, 5000, request.url, comm_Obj['dbg']);
                    callback('0', {}, '', request, response, body_Obj);
                    return '0';
                }
            });
        }
    }
}

function check_rt_query(request, response, body_Obj, callback) {
    //var ri = url.parse(request.url).pathname;

    //var url_arr = ri.split('/');
    //var last_url = url_arr[url_arr.length-1];
    //var op = 'direct';

    if (request.query.real == 4) {
        var check_Obj = {};
        check_Obj.ty = '3';
        callback('1', check_Obj, 'direct', request, response, body_Obj);
        return'1';
    }

    if (request.query.rt == 3) { // default, blocking
        check_resource(request, response, body_Obj, function (rsc, check_Obj, op, request, response, body_Obj) {
            callback(rsc, check_Obj, op, request, response, body_Obj);
        });
    }
    else if (request.query.rt == 1 || request.query.rt == 2) { // nonblocking
        if(request.query.rt == 2 && request.headers['x-m2m-rtu'] == null && request.headers['x-m2m-rtu'] == '') {
            body_Obj = {};
            body_Obj['dbg'] = 'X-M2M-RTU is none';
            responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
            callback('0', {}, '', request, response, body_Obj);
            return '0';
        }

        // first create request resource under CSEBase
        var temp_rootnm = request.headers.rootnm;
        //var temp_rt = request.query.rt;
        var ty = '17';
        var rt_body_Obj = {req: {}};
        request.headers.rootnm = Object.keys(rt_body_Obj)[0];
        request.query.rt = 3;
        resource.create(request, response, ty, rt_body_Obj, function (rsc) {
            if (rsc == '1') {
                request.headers.rootnm = temp_rootnm;
                request.query.rt = 1;
                check_resource(request, response, body_Obj, function (rsc, parent_comm, op, request, response, body_Obj) {
                    callback(rsc, parent_comm, op, request, response, body_Obj);
                });
            }
        });
    }
    else {
        body_Obj = {};
        body_Obj['dbg'] = 'OPERATION_NOT_ALLOWED (rt query is not supported)';
        responder.response_result(request, response, 405, body_Obj, 4005, request.url, body_Obj['dbg']);
        callback('0', {}, '', request, response, body_Obj);
        return '0';
    }
}

function check_grp(request, response, ri, callback) {
    db_sql.select_grp(ri, function (err, result_Obj) {
        if (!err) {
            if (result_Obj.length == 1) {
                result_Obj[0].macp = JSON.parse(result_Obj[0].macp);
                result_Obj[0].mid = JSON.parse(result_Obj[0].mid);

                if(result_Obj[0].mid.length == 0) {
                    result_Obj = {};
                    result_Obj['dbg'] = 'NO_MEMBERS: memberID in parent group is empty';
                    responder.response_result(request, response, 403, result_Obj, 4109, request.url, result_Obj['dbg']);
                    callback('0');
                    return '0';
                }
                else {
                    callback('1', result_Obj[0]);
                }
            }
            else {
                result_Obj = {};
                result_Obj['dbg'] = 'resource does not exist';
                responder.response_result(request, response, 404, result_Obj, 4004, request.url, result_Obj['dbg']);
                callback('0');
                return '0';
            }
        }
        else {
            result_Obj = {};
            result_Obj['dbg'] = result_Obj.message;
            responder.response_result(request, response, 500, result_Obj, 5000, request.url, result_Obj['dbg']);
            callback('0');
            return '0';
        }
    });
}

/**
 *
 * @param request
 * @param response
 */
function lookup_create(request, response) {
    check_http(request, response, function (ty, body_Obj, request, response) {
        if (ty == '0') {
            return ty;
        }
        check_rt_query(request, response, body_Obj, function (rsc, parent_comm, op, request, response, body_Obj) {
            if (rsc == '0') {
                return rsc;
            }

            var rootnm = request.headers.rootnm;

            tr.check(request, parent_comm.ri, body_Obj, function (rsc, body_Obj) {
                if (rsc == '0') {
                    body_Obj = {};
                    body_Obj['dbg'] = resultStatusCode['4230'];
                    responder.response_result(request, response, 423, body_Obj, 4230, request.url, resultStatusCode['4230']);
                    return '0';
                }

                if (op == 'fanoutpoint') {
                    // check access right for fanoutpoint
                    check_grp(request, response, parent_comm.ri, function (rsc, result_grp) {
                        if (rsc == '0') {
                            return rsc;
                        }

                        security.check(request, response, parent_comm.ty, result_grp.macp, '1', result_grp.cr, function (rsc, request, response) {
                            if (rsc == '0') {
                                body_Obj = {};
                                body_Obj['dbg'] = resultStatusCode['4103'];
                                responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                                return '0';
                            }

                            fopt.check(request, response, result_grp, ty, body_Obj);
                        });
                    });
                }
                else { //if(op == 'direct') {
                    if(request.query.real == 4) {

                    }

                    if ((ty == 1) && (parent_comm.ty == 5 || parent_comm.ty == 16 || parent_comm.ty == 2)) { // accessControlPolicy
                    }
                    else if ((ty == 9) && (parent_comm.ty == 5 || parent_comm.ty == 16 || parent_comm.ty == 2)) { // group
                    }
                    else if ((ty == 16) && (parent_comm.ty == 5)) { // remoteCSE
                        if (usecsetype == 'asn' && request.headers.csr == null) {
                            body_Obj = {};
                            body_Obj['dbg'] = 'ASN CSE can not have child CSE (remoteCSE)';
                            responder.response_result(request, response, 400, body_Obj, 4000, request.url, body_Obj['dbg']);
                            return '0';
                        }
                    }
                    else if ((ty == 10) && (parent_comm.ty == 5)) { // locationPolicy
                    }
                    else if ((ty == 2) && (parent_comm.ty == 5)) { // ae
                    }
                    else if ((ty == 3) && (parent_comm.ty == 5 || parent_comm.ty == 2 || parent_comm.ty == 3)) { // container
                    }
                    else if ((ty == 23) && (parent_comm.ty == 5 || parent_comm.ty == 16 || parent_comm.ty == 2 ||
                            parent_comm.ty == 3 || parent_comm.ty == 24 || parent_comm.ty == 29 || parent_comm.ty == 9 || parent_comm.ty == 1 || parent_comm.ty == 27)) { // sub
                    }
                    else if ((ty == 4) && (parent_comm.ty == 3)) { // contentInstance
                    }
                    else if ((ty == 24) && (parent_comm.ty == 2 || parent_comm.ty == 3 || parent_comm.ty == 4 || parent_comm.ty == 29)) { // semanticDescriptor
                    }
                    else if ((ty == 29) && (parent_comm.ty == 5 || parent_comm.ty == 16 || parent_comm.ty == 2)) { // timeSeries
                    }
                    else if ((ty == 30) && (parent_comm.ty == 29)) { // timeSeriesInstance
                        //body_Obj[rootnm].mni = parent_comm.mni;
                    }
                    else if ((ty == 27) && (parent_comm.ty == 2 || parent_comm.ty == 16)) { // multimediaSession
                    }
                    else if ((ty == 14) && (parent_comm.ty == 5)) { // node
                    }
                    else if ((ty == 13) && (parent_comm.ty == 14)) { // mgmtObj
                    }
                    else if ((ty == 38) && (parent_comm.ty == 5 || parent_comm.ty == 16 || parent_comm.ty == 2 ||
                            parent_comm.ty == 3 || parent_comm.ty == 24 || parent_comm.ty == 29 || parent_comm.ty == 9 || parent_comm.ty == 1 || parent_comm.ty == 27)) { // transaction
                    }
                    else if ((ty == 39) && (parent_comm.ty == 5 || parent_comm.ty == 16 || parent_comm.ty == 2 ||
                            parent_comm.ty == 3 || parent_comm.ty == 24 || parent_comm.ty == 29 || parent_comm.ty == 9 || parent_comm.ty == 1 || parent_comm.ty == 27)) { // transaction
                    }
                    else {
                        body_Obj = {};
                        body_Obj['dbg'] = 'TARGET_NOT_SUBSCRIBABLE: request ty creating can not create under parent resource';
                        responder.response_result(request, response, 403, body_Obj, 5203, request.url, body_Obj['dbg']);
                        return '0';
                    }

                    // for security with acp
                    //if (!body_Obj[rootnm].acpi) {
                    //    body_Obj[rootnm].acpi = [];
                    //}

                    // 20171212 remove inherit acp of parent to current resource
                    // for (var index in parent_comm.acpi) {
                    //     if (parent_comm.acpi.hasOwnProperty(index)) {
                    //         body_Obj[rootnm].acpi.push(parent_comm.acpi[index]);
                    //     }
                    // }

                    //if (parent_comm.ty == 2 || parent_comm.ty == 4 || parent_comm.ty == 3 || parent_comm.ty == 9 || parent_comm.ty == 24 || parent_comm.ty == 23 || parent_comm.ty == 29) {
                    //db_sql.select_resource(responder.typeRsrc[parent_comm.ty], parent_comm.ri, function (err, parent_comm) {
                        //if (!err) {
                            if (((ty == 4) && (parent_comm.ty == 3)) || ((ty == 30) && (parent_comm.ty == 29))) { // contentInstance
                                if (parent_comm.mni == null) {
                                    //body_Obj[rootnm].mni = '3153600000';
                                }
                                else {
                                    if (parseInt(parent_comm.mni) == 0) {
                                        body_Obj = {};
                                        body_Obj['dbg'] = 'can not create cin because mni value is zero';
                                        responder.response_result(request, response, 406, body_Obj, 5207, request.url, body_Obj['dbg']);
                                        return '0';
                                    }
                                    else if (parseInt(parent_comm.mbs) == 0) {
                                        body_Obj = {};
                                        body_Obj['dbg'] = 'can not create cin because mbs value is zero';
                                        responder.response_result(request, response, 406, body_Obj, 5207, request.url, body_Obj['dbg']);
                                        return '0';
                                    }
                                    else if (parent_comm.disr == true) {
                                        body_Obj = {};
                                        body_Obj['dbg'] = 'OPERATION NOT ALLOWED: disr attribute is true';
                                        responder.response_result(request, response, 405, body_Obj, 4005, request.url, body_Obj['dbg']);
                                        return '0';
                                    }
                                    else {
                                        //body_Obj[rootnm].mni = parent_spec[0].mni;
                                    }
                                }

                                request.headers.mni = parent_comm.mni;
                                request.headers.mbs = parent_comm.mbs;
                                request.headers.cni = parent_comm.cni;
                                request.headers.cbs = parent_comm.cbs;
                                request.headers.st = parent_comm.st;
                            }

                            if (parent_comm.length == 0) {
                                parent_comm = {};
                                parent_comm.cr = '';
                                console.log('no creator');
                            }
                            else {
                                if (parent_comm.ty == 2) {
                                    parent_comm.cr = parent_comm.aei;
                                }
                            }

                            if (ty == 23) {
                                var access_value = '3';
                            }
                            else {
                                access_value = '1';
                            }

                            security.check(request, response, parent_comm.ty, parent_comm.acpi, access_value, parent_comm.cr, function (rsc, request, response) {
                                if (rsc == '0') {
                                    body_Obj = {};
                                    body_Obj['dbg'] = resultStatusCode['4103'];
                                    responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                                    return '0';
                                }
                                resource.create(request, response, ty, body_Obj, function (rsc) {

                                });
                            });
                        // }
                        // else {
                        //     body_Obj = {};
                        //     body_Obj['dbg'] = 'select resource error in security';
                        //     responder.response_result(request, response, 500, search_Obj, 5000, request.url, body_Obj['dbg']);
                        //     callback('0', search_Obj);
                        //     return '0';
                        // }
                    //});
                    // }
                    // else {
                    //     if (ty == 23) {
                    //         var access_value = '3';
                    //     }
                    //     else {
                    //         access_value = '1';
                    //     }
                    //     security.check(request, response, parent_comm.ty, parent_comm.acpi, access_value, '', function (rsc, request, response) {
                    //         if (rsc == '0') {
                    //             body_Obj = {};
                    //             body_Obj['dbg'] = resultStatusCode['4103'];
                    //             responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                    //             return '0';
                    //         }
                    //         resource.create(request, response, ty, body_Obj, function (rsc) {
                    //
                    //         });
                    //     });
                    // }
                }
            });
        });
    });
}

function lookup_retrieve(request, response) {
    check_http(request, response, function (option, body_Obj, request, response) {
        if (option == '0') {
            return option;
        }
        check_rt_query(request, response, body_Obj, function (rsc, result_Obj, op, request, response, body_Obj) {
            if (rsc == '0') {
                return rsc;
            }

            tr.check(request, result_Obj.ri, body_Obj, function (rsc, body_Obj) {
                if (rsc == '0') {
                    body_Obj = {};
                    body_Obj['dbg'] = resultStatusCode['4230'];
                    responder.response_result(request, response, 423, body_Obj, 4230, request.url, resultStatusCode['4230']);
                    return '0';
                }

                if (op == 'fanoutpoint') {
                    // check access right for fanoutpoint
                    check_grp(request, response, result_Obj.ri, function (rsc, result_grp) {
                        if (rsc == '0') {
                            return rsc;
                        }

                        if (request.query.fu == 1) {
                            security.check(request, response, result_Obj.ty, result_grp.macp, '32', result_grp.cr, function (rsc, request, response) {
                                if (rsc == '0') {
                                    body_Obj = {};
                                    body_Obj['dbg'] = resultStatusCode['4103'];
                                    responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                                    return '0';
                                }

                                fopt.check(request, response, result_grp, body_Obj);
                            });
                        }
                        else {
                            security.check(request, response, result_Obj.ty, result_grp.macp, '2', result_grp.cr, function (rsc, request, response) {
                                if (rsc == '0') {
                                    body_Obj = {};
                                    body_Obj['dbg'] = resultStatusCode['4103'];
                                    responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                                    return '0';
                                }

                                fopt.check(request, response, result_grp, body_Obj);
                            });
                        }

                    });
                }
                else { //if(op == 'direct') {
                    if (result_Obj.ty == 2) {
                        result_Obj.cr = result_Obj.aei;
                    }
                    else if (result_Obj.ty == 16) {
                        result_Obj.cr = result_Obj.csi;
                    }

                    if (request.query.fu == 1) {
                        security.check(request, response, result_Obj.ty, result_Obj.acpi, '32', result_Obj.cr, function (rsc, request, response) {
                            if (rsc == '0') {
                                body_Obj = {};
                                body_Obj['dbg'] = resultStatusCode['4103'];
                                responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                                return '0';
                            }
                            resource.retrieve(request, response, result_Obj);
                        });
                    }
                    else {
                        security.check(request, response, result_Obj.ty, result_Obj.acpi, '2', result_Obj.cr, function (rsc, request, response) {
                            if (rsc == '0') {
                                body_Obj = {};
                                body_Obj['dbg'] = resultStatusCode['4103'];
                                responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                                return '0';
                            }
                            resource.retrieve(request, response, result_Obj);
                        });
                    }
                }
            });
        });
    });
}

function lookup_update(request, response) {
    check_http(request, response, function (option, body_Obj, request, response) {
        if (option == '0') {
            return option;
        }
        check_rt_query(request, response, body_Obj, function (rsc, results_comm, op, request, response, body_Obj) {
            if (rsc == '0') {
                return rsc;
            }

            tr.check(request, results_comm.ri, body_Obj, function (rsc, body_Obj) {
                if (rsc == '0') {
                    body_Obj = {};
                    body_Obj['dbg'] = resultStatusCode['4230'];
                    responder.response_result(request, response, 423, body_Obj, 4230, request.url, resultStatusCode['4230']);
                    return '0';
                }

                if (op == 'fanoutpoint') {
                    // check access right for fanoutpoint
                    check_grp(request, response, results_comm.ri, function (rsc, result_grp) {
                        if (rsc == '0') {
                            return rsc;
                        }

                        security.check(request, response, results_comm.ty, result_grp.macp, '4', result_grp.cr, function (rsc, request, response) {
                            if (rsc == '0') {
                                body_Obj = {};
                                body_Obj['dbg'] = resultStatusCode['4103'];
                                responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                                return '0';
                            }

                            fopt.check(request, response, result_grp, results_comm.ty, body_Obj);
                        });
                    });
                }
                else { //if(op == 'direct') {
                    //if(results_comm.ty == 2 || results_comm.ty == 4 || results_comm.ty == 3 || results_comm.ty == 9 || results_comm.ty == 16 || results_comm.ty == 24 ||
                    //    results_comm.ty == 23 || results_comm.ty == 29 || results_comm.ty == 38 || results_comm.ty == 39) {
                    db_sql.select_resource(responder.typeRsrc[results_comm.ty], results_comm.ri, function (err, results_spec) {
                        if (!err) {
                            if (results_spec.length == 0) {
                                results_spec[0] = {};
                                results_spec[0].cr = '';
                                console.log('no creator');
                            }
                            else {
                                if (results_comm.ty == 2) {
                                    results_spec[0].cr = results_spec[0].aei;
                                }
                                else if (results_comm.ty == 16) {
                                    results_spec[0].cr = results_spec[0].csi;
                                }
                            }

                            var acpi_check = 0;
                            var other_check = 0;
                            for(var rootnm in body_Obj) {
                                if(body_Obj.hasOwnProperty(rootnm)) {
                                    for(var attr in body_Obj[rootnm]) {
                                        if(body_Obj[rootnm].hasOwnProperty(attr)) {
                                            if(attr == 'acpi') {
                                                acpi_check++;
                                            }
                                            else {
                                                other_check++;
                                            }
                                        }
                                    }
                                }
                            }

                            if(other_check > 0) {
                                security.check(request, response, results_comm.ty, results_comm.acpi, '4', results_spec[0].cr, function (rsc, request, response) {
                                    if (rsc == '0') {
                                        body_Obj = {};
                                        body_Obj['dbg'] = resultStatusCode['4103'];
                                        responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                                        return '0';
                                    }
                                    resource.update(request, response, results_comm, body_Obj);
                                });
                            }
                            else {
                                resource.update(request, response, results_comm, body_Obj);
                            }
                        }
                        else {
                            body_Obj = {};
                            body_Obj['dbg'] = 'select resource error in security';
                            responder.response_result(request, response, 500, search_Obj, 5000, request.url, body_Obj['dbg']);
                            callback('0', search_Obj);
                            return '0';
                        }
                    });
                    // }
                    // else {
                    //     security.check(request, response, results_comm.ty, results_comm.acpi, '4', '', function (rsc, request, response) {
                    //         if (rsc == '0') {
                    //             body_Obj = {};
                    //             body_Obj['dbg'] = resultStatusCode['4103'];
                    //             responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                    //             return '0';
                    //         }
                    //         resource.update(request, response, results_comm, body_Obj);
                    //     });
                    // }
                }
            });
        });
    });
}

function lookup_delete(request, response) {
    check_http(request, response, function (option, body_Obj, request, response) {
        if (option == '0') {
            return option;
        }
        check_rt_query(request, response, body_Obj, function (rsc, results_comm, op, request, response, body_Obj) {
            if (rsc == '0') {
                return rsc;
            }

            tr.check(request, results_comm.ri, body_Obj, function (rsc, body_Obj) {
                if (rsc == '0') {
                    var body_Obj = {};
                    body_Obj['dbg'] = resultStatusCode['4230'];
                    responder.response_result(request, response, 423, body_Obj, 4230, request.url, resultStatusCode['4230']);
                    return '0';
                }

                if (op == 'fanoutpoint') {
                    // check access right for fanoutpoint
                    check_grp(request, response, results_comm.ri, function (rsc, result_grp) {
                        if (rsc == '0') {
                            return rsc;
                        }

                        security.check(request, response, results_comm.ty, result_grp.macp, '8', result_grp.cr, function (rsc, request, response) {
                            if (rsc == '0') {
                                body_Obj = {};
                                body_Obj['dbg'] = resultStatusCode['4103'];
                                responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                                return '0';
                            }

                            fopt.check(request, response, result_grp, results_comm.ty, body_Obj);
                        });
                    });
                }
                else { //if(op == 'direct') {
                    //if(results_comm.ty == 2 || results_comm.ty == 4 || results_comm.ty == 3 || results_comm.ty == 9 || results_comm.ty == 16 || results_comm.ty == 24 ||
                    //    results_comm.ty == 23 || results_comm.ty == 29 || results_comm.ty == 38 || results_comm.ty == 39) {
                    db_sql.select_resource(responder.typeRsrc[results_comm.ty], results_comm.ri, function (err, results_spec) {
                        if (!err) {
                            if (results_spec.length == 0) {
                                results_spec[0] = {};
                                results_spec[0].cr = '';
                                console.log('no creator');
                            }
                            else {
                                if (results_comm.ty == 2) {
                                    results_spec[0].cr = results_spec[0].aei;
                                }
                                else if (results_comm.ty == 16) {
                                    results_spec[0].cr = results_spec[0].csi;
                                }
                            }
                            security.check(request, response, results_comm.ty, results_comm.acpi, '8', results_spec[0].cr, function (rsc, request, response) {
                                if (rsc == '0') {
                                    body_Obj = {};
                                    body_Obj['dbg'] = resultStatusCode['4103'];
                                    responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                                    return '0';
                                }

                                makeObject(results_spec[0]);
                                results_comm = merge(results_comm, results_spec[0]);

                                for(var idx in cbs_cache) {
                                    if(cbs_cache.hasOwnProperty(idx)) {
                                        if(idx.includes(results_comm.ri)) {
                                            delete cbs_cache[idx];
                                            del_cbs_cache(idx);
                                        }
                                    }
                                }

                                resource.delete(request, response, results_comm);
                            });
                        }
                        else {
                            body_Obj = {};
                            body_Obj['dbg'] = 'select resource error in security';
                            responder.response_result(request, response, 500, search_Obj, 5000, request.url, body_Obj['dbg']);
                            callback('0', search_Obj);
                            return '0';
                        }
                    });
                    // }
                    // else {
                    //     security.check(request, response, results_comm.ty, results_comm.acpi, '8', '', function (rsc, request, response) {
                    //         if (rsc == '0') {
                    //             body_Obj = {};
                    //             body_Obj['dbg'] = resultStatusCode['4103'];
                    //             responder.response_result(request, response, 403, body_Obj, 4103, request.url, resultStatusCode['4103']);
                    //             return '0';
                    //         }
                    //         resource.delete(request, response, results_comm);
                    //     });
                    // }
                }
            });
        });
    });
}


function updateHitCount(binding) {
    try {
        var _hit = get_all_hit_cache();

        var a = moment().utc();
        var cur_t = a.format('YYYYMMDD');
        var h = a.hours();

        if (_hit.hasOwnProperty(cur_t)) {
            _hit[cur_t][h][binding]++;

            //console.log(hit);
            set_hit_cache(cur_t, _hit[cur_t]);
        }
        else {
            _hit[cur_t] = [];
            for (var i = 0; i < 24; i++) {
                _hit[cur_t].push({});
            }
            _hit[cur_t][h][binding]++;

            set_hit_cache(cur_t, _hit[cur_t]);
        }
    }
    catch (e) {
        console.log('[updateHitCount] ' + e.message);
    }
}

global.elapsed_hrstart = {};

var onem2mParser = bodyParser.text(
    {
        limit: '1mb',
        type: 'application/onem2m-resource+xml;application/xml;application/json;application/vnd.onem2m-res+xml;application/vnd.onem2m-res+json'
    }
);
//var onem2mParser = bodyParser.text({ limit: '1mb', type: '*/*' });


//////// contribution code
// Kevin Lee, Executive Director, Unibest INC, Owner of Howchip.com
// Process for CORS problem
app.use(function (req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET, PUT, POST, DELETE, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, X-M2M-RI, X-M2M-RVI, X-M2M-RSC, Accept, X-M2M-Origin, Locale');
    res.header('Access-Control-Expose-Headers', 'Origin, X-Requested-With, Content-Type, X-M2M-RI, X-M2M-RVI, X-M2M-RSC, Accept, X-M2M-Origin, Locale');
    (req.method == 'OPTIONS') ? res.sendStatus(200) : next();
});


// remoteCSE, ae, cnt
app.post(onem2mParser, function (request, response) {
    // request.headers.elapsed_tid = require('shortid').generate();
    // elapsed_hrstart[request.headers.elapsed_tid] = process.hrtime();
    // console.time(request.headers.elapsed_tid);

    var fullBody = '';
    request.on('data', function (chunk) {
        fullBody += chunk.toString();
    });
    request.on('end', function () {
        if (request.headers.hasOwnProperty('binding')) {
            updateHitCount(request.headers['binding']);
        }
        else {
            updateHitCount('H');
        }

        request.body = fullBody;
        if (request.query.fu == null) {
            request.query.fu = 2;
        }
        if (request.query.rcn == null) {
            request.query.rcn = 1;
        }
        if (request.query.rt == null) {
            request.query.rt = 3;
        }

        check_body_format(request);

        var absolute_url = request.url.replace('\/_\/', '\/\/').split('#')[0];
        absolute_url = absolute_url.replace(usespid, '/~');
        absolute_url = absolute_url.replace(/\/~\/[^\/]+\/?/, '/');
        var absolute_url_arr = absolute_url.split('/');

        console.log('\n' + request.method + ' : ' + request.url);
        //console.log('HTTP BODY: ' + request.body);
        db_sql.get_ri_sri(request, response, absolute_url_arr[1].split('?')[0], function (err, results, request, response) {
            if (err) {
                responder.error_result(request, response, 500, 5000, 'database error (can not get resourceID from database)');
            }
            else {
                absolute_url = (results.length == 0) ? absolute_url : ((results[0].hasOwnProperty('ri')) ? absolute_url.replace('/' + absolute_url_arr[1], results[0].ri) : absolute_url);

                check_notification(request, absolute_url, function(status, http_code, rsc_code, caption) {
                    if(status == 'notify') {
                        check_ae(absolute_url, request, response);

                    }
                    else if(status == 'post') {
                        if (url.parse(absolute_url).pathname.split('/')[1] == usecsebase) {
                            request.url = absolute_url;
                            if ((request.query.fu == 2) &&
                                (request.query.rcn == 0 || request.query.rcn == 1 || request.query.rcn == 2 || request.query.rcn == 3)) {
                                lookup_create(request, response);
                            }
                            else {
                                responder.error_result(request, response, http_code, rsc_code, 'rcn or fu query is not supported at POST request');
                            }
                        }
                        else {
                            check_csr(absolute_url, request, response);
                        }
                    }
                    else if(status == '0') {
                        responder.error_result(request, response, http_code, rsc_code, caption);
                    }
                });
            }
        });
    });
});

app.get(onem2mParser, function (request, response) {
    // elapsed_tid = require('shortid').generate();
    // elapsed_hrstart[elapsed_tid] = process.hrtime();
    // console.time(elapsed_tid);

    var fullBody = '';
    request.on('data', function (chunk) {
        fullBody += chunk.toString();
    });
    request.on('end', function () {
        if (request.headers.hasOwnProperty('binding')) {
            updateHitCount(request.headers['binding']);
        }
        else {
            updateHitCount('H');
        }

        request.body = fullBody;
        if (request.query.fu == null) {
            request.query.fu = 2;
        }
        if (request.query.rcn == null) {
            request.query.rcn = 1;
        }
        if (request.query.rt == null) {
            request.query.rt = 3;
        }

        check_body_format(request);

        request.url = request.url.replace('%23', '#'); // convert '%23' to '#' of url
        request.hash = url.parse(request.url).hash;

        var absolute_url = request.url.replace('\/_\/', '\/\/').split('#')[0];
        absolute_url = absolute_url.replace(usespid, '/~');
        absolute_url = absolute_url.replace(/\/~\/[^\/]+\/?/, '/');
        var absolute_url_arr = absolute_url.split('/');

        console.log('\n' + request.method + ' : ' + request.url);
        //console.log('HTTP BODY: ' + request.body);
        db_sql.get_ri_sri(request, response, absolute_url_arr[1].split('?')[0], function (err, results, request, response) {
            if (err) {
                responder.error_result(request, response, 500, 5000, 'database error (can not get resourceID from database)');
            }
            else {
                absolute_url = (results.length == 0) ? absolute_url : ((results[0].hasOwnProperty('ri')) ? absolute_url.replace('/' + absolute_url_arr[1], results[0].ri) : absolute_url);

                if (url.parse(absolute_url).pathname == '/hit') {
                    var _hit = get_all_hit_cache();
                    response.status(200).end(JSON.stringify(_hit, null, 4));
                    return;
                }

                if (url.parse(absolute_url).pathname == '/total_ae') {
                    db_sql.select_sum_ae(function (err, result) {
                        if(!err) {
                            var total_ae = result[0];
                            response.status(200).end(JSON.stringify(total_ae, null, 4));
                        }
                    });
                    return;
                }

                if (url.parse(absolute_url).pathname == '/total_cbs') {
                    db_sql.select_sum_cbs(function (err, result) {
                        if(!err) {
                            var total_cbs = result[0];
                            response.status(200).end(JSON.stringify(total_cbs, null, 4));
                        }
                    });
                    return;
                }

                if (url.parse(absolute_url).pathname.split('/')[1] == usecsebase) {
                    request.url = absolute_url;
                    if ((request.query.fu == 1 || request.query.fu == 2) &&
                        (request.query.rcn == 1 || request.query.rcn == 4 || request.query.rcn == 5 || request.query.rcn == 6 || request.query.rcn == 7)) {
                        lookup_retrieve(request, response);
                    }
                    else {
                        responder.error_result(request, response, 400, 4000, 'BAD_REQUEST (rcn or fu query is not supported at GET request)');
                    }
                }
                else {
                    check_csr(absolute_url, request, response);
                }
            }
        });
    });
});


app.put(onem2mParser, function (request, response) {
    // elapsed_tid = require('shortid').generate();
    // elapsed_hrstart[elapsed_tid] = process.hrtime();
    // console.time(elapsed_tid);

    var fullBody = '';
    request.on('data', function (chunk) {
        fullBody += chunk.toString();
    });
    request.on('end', function () {
        if (request.headers.hasOwnProperty('binding')) {
            updateHitCount(request.headers['binding']);
        }
        else {
            updateHitCount('H');
        }

        request.body = fullBody;
        if (request.query.fu == null) {
            request.query.fu = 2;
        }
        if (request.query.rcn == null) {
            request.query.rcn = 1;
        }
        if (request.query.rt == null) {
            request.query.rt = 3;
        }

        check_body_format(request);

        var absolute_url = request.url.replace('\/_\/', '\/\/').split('#')[0];
        absolute_url = absolute_url.replace(usespid, '/~');
        absolute_url = absolute_url.replace(/\/~\/[^\/]+\/?/, '/');
        var absolute_url_arr = absolute_url.split('/');

        console.log('\n' + request.method + ' : ' + request.url);
        //console.log('HTTP BODY: ' + request.body);
        db_sql.get_ri_sri(request, response, absolute_url_arr[1].split('?')[0], function (err, results, request, response) {
            if (err) {
                responder.error_result(request, response, 500, 5000, 'database error (can not get resourceID from database)');
            }
            else {
                absolute_url = (results.length == 0) ? absolute_url : ((results[0].hasOwnProperty('ri')) ? absolute_url.replace('/' + absolute_url_arr[1], results[0].ri) : absolute_url);

                if (url.parse(absolute_url).pathname == ('/' + usecsebase)) {
                    responder.error_result(request, response, 405, 4005, 'OPERATION_NOT_ALLOWED');
                }
                else if (url.parse(absolute_url).pathname.split('/')[1] == usecsebase) {
                    request.url = absolute_url;
                    if ((request.query.fu == 2) &&
                        (request.query.rcn == 0 || request.query.rcn == 1)) {
                        lookup_update(request, response);
                    }
                    else {
                        responder.error_result(request, response, 400, 4000, 'rcn query is not supported at PUT request');
                    }
                }
                else {
                    check_csr(absolute_url, request, response);
                }
            }
        });
    });
});

app.delete(onem2mParser, function (request, response) {
    // elapsed_tid = require('shortid').generate();
    // elapsed_hrstart[elapsed_tid] = process.hrtime();
    // console.time(elapsed_tid);

    var fullBody = '';
    request.on('data', function (chunk) {
        fullBody += chunk.toString();
    });
    request.on('end', function () {
        if (request.headers.hasOwnProperty('binding')) {
            updateHitCount(request.headers['binding']);
        }
        else {
            updateHitCount('H');
        }

        request.body = fullBody;
        if (request.query.fu == null) {
            request.query.fu = 2;
        }
        if (request.query.rcn == null) {
            request.query.rcn = 1;
        }
        if (request.query.rt == null) {
            request.query.rt = 3;
        }

        check_body_format(request);

        var absolute_url = request.url.replace('\/_\/', '\/\/').split('#')[0];
        absolute_url = absolute_url.replace(usespid, '/~');
        absolute_url = absolute_url.replace(/\/~\/[^\/]+\/?/, '/');
        var absolute_url_arr = absolute_url.split('/');

        console.log('\n' + request.method + ' : ' + request.url);
        //console.log('HTTP BODY: ' + request.body);
        db_sql.get_ri_sri(request, response, absolute_url_arr[1].split('?')[0], function (err, results, request, response) {
            if (err) {
                responder.error_result(request, response, 500, 5000, 'database error (can not get resourceID from database)');
            }
            else {
                absolute_url = (results.length == 0) ? absolute_url : ((results[0].hasOwnProperty('ri')) ? absolute_url.replace('/' + absolute_url_arr[1], results[0].ri) : absolute_url);

                if (url.parse(absolute_url).pathname == ('/' + usecsebase)) {
                    responder.error_result(request, response, 405, 4005, 'OPERATION_NOT_ALLOWED');
                }
                else if (url.parse(absolute_url).pathname.split('/')[1] == usecsebase) {
                    request.url = absolute_url;
                    if ((request.query.fu == 2) &&
                        (request.query.rcn == 0 || request.query.rcn == 1)) {
                        lookup_delete(request, response);
                    }
                    else {
                        responder.error_result(request, response, 400, 4000, 'rcn query is not supported at DELETE request');
                    }
                }
                else {
                    check_csr(absolute_url, request, response);
                }
            }
        });
    });
});

function check_notification(request, url, callback) {
    if(request.headers.hasOwnProperty('content-type')) {
        if(request.headers['content-type'].includes('ty')) { // post
            callback('post');
        }
        else {
            if(request.headers['content-type'].includes('xml')) {
                request.headers.usebodytype = 'xml';
                try {
                    var parser = new xml2js.Parser({explicitArray: false});
                    parser.parseString(request.body.toString(), function (err, body_Obj) {
                        if (err) {
                            callback('0', 400, 4000, 'do not parse xml body' + err.message);
                        }
                        else {
                            var rootnm = Object.keys(body_Obj)[0].split(':')[1];
                            if(rootnm == 'sgn') {
                                callback('notify');
                            }
                            else {
                                callback('0', 400, 4000, 'ty is none in content-type header');
                            }
                        }
                    });
                }
                catch(e) {
                    callback('0', 400, 4000, 'do not parse xml body' + e.message);
                }
            }
            else if(request.headers['content-type'].includes('cbor')) {
                request.headers.usebodytype = 'cbor';
                try {
                    var encoded = request.body;
                    cbor.decodeFirst(encoded, function(err, body_Obj) {
                        if (err) {
                            callback('0', 400, 4000, 'do not parse cbor body');
                        }
                        else {
                            var rootnm = Object.keys(body_Obj)[0].split(':')[1];
                            if(rootnm == 'sgn') {
                                callback('notify');
                            }
                            else {
                                callback('0', 400, 4000, 'ty is none in content-type header');
                            }
                        }
                    });
                }
                catch(e) {
                    callback('0', 400, 4000, 'do not parse cbor body');
                }
            }
            else {
                request.headers.usebodytype = 'json';
                try {
                    var body_Obj = JSON.parse(request.body.toString());
                    var rootnm = Object.keys(body_Obj)[0].split(':')[1];
                    if(rootnm == 'sgn') {
                        callback('notify');
                    }
                    else {
                        callback('0', 400, 4000, 'ty is none in content-type header');
                    }
                }
                catch (e) {
                    callback('0', 400, 4000, 'do not parse json body');
                }
            }
        }
    }
    else {
        callback('0', 400, 4000, 'content-type is none');
    }
}

function check_ae(absolute_url, request, response) {
    var ri = absolute_url;
    console.log('[check_ae] : ' + ri);
    db_sql.select_ae(ri, function (err, result_ae) {
        if (!err) {
            if (result_ae.length == 1) {
                var point = {};
                var poa_arr = JSON.parse(result_ae[0].poa);
                for (var i = 0; i < poa_arr.length; i++) {
                    var poa = url.parse(poa_arr[i]);
                    if (poa.protocol == 'http:') {
                        console.log('send notification to ' + poa_arr[i]);

                        notify_http(poa.hostname, poa.port, poa.path, request, response);
                    }
                    else if (poa.protocol == 'coap:') {
                        console.log('send notification to ' + poa_arr[i]);

                        notify_http(poa.hostname, poa.port, poa.path, request, response);
                    }
                    else if (poa.protocol == 'mqtt:') {
                        responder.error_result(request, response, 500, 5000, 'notification with mqtt is not supported');
                    }
                    else if (poa.protocol == 'ws:') {
                        responder.error_result(request, response, 500, 5000, 'notification with mqtt is not supported');
                    }
                    else {
                        point = {};
                        point['dbg'] = 'protocol(' + poa.protocol + ') in poa of ae is not supported';
                        responder.error_result(request, response, 400, 4000, point['dbg']);
                    }
                }
            }
            else {
                point = {};
                point['dbg'] = 'ae is not found';
                responder.error_result(request, response, 400, 4000, point['dbg']);
            }
        }
        else {
            console.log('[check_ae] query error: ' + result_ae.message);
        }
    });
}

function check_csr(absolute_url, request, response) {
    var ri = util.format('/%s/%s', usecsebase, url.parse(absolute_url).pathname.split('/')[1]);
    console.log('[check_csr] : ' + ri);
    db_sql.select_csr(ri, function (err, result_csr) {
        if (!err) {
            if (result_csr.length == 1) {
                var point = {};
                point.forwardcbname = result_csr[0].cb.replace('/', '');
                var poa_arr = JSON.parse(result_csr[0].poa);
                for (var i = 0; i < poa_arr.length; i++) {
                    var poa = url.parse(poa_arr[i]);
                    if (poa.protocol == 'http:') {
                        point.forwardcbhost = poa.hostname;
                        point.forwardcbport = poa.port;

                        console.log('csebase forwarding to ' + point.forwardcbname);

                        forward_http(point.forwardcbhost, point.forwardcbport, request, response);
                    }
                    else if (poa.protocol == 'mqtt:') {
                        point.forwardcbmqtt = poa.hostname;

                        responder.error_result(request, response, 500, 5000, 'forwarding with mqtt is not supported');
                    }
                    else {
                        point = {};
                        point['dbg'] = 'protocol(' + poa.protocol + ') in poa of csr is not supported';
                        responder.error_result(request, response, 400, 4000, point['dbg']);
                    }
                }
            }
            else {
                point = {};
                point['dbg'] = 'csebase is not found';
                responder.error_result(request, response, 400, 4000, point['dbg']);
            }
        }
        else {
            console.log('[check_csr] query error: ' + result_csr.message);
        }
    });
}


function notify_http(hostname, port, path, request, response) {
    var options = {
        hostname: hostname,
        port: port,
        path: path,
        method: request.method,
        headers: request.headers
    };

    var req = http.request(options, function (res) {
        var fullBody = '';
        res.on('data', function (chunk) {
            fullBody += chunk.toString();
        });

        res.on('end', function () {
            console.log('--------------------------------------------------------------------------');
            //console.log(res.url);
            //console.log(res.headers);
            console.log(fullBody);
            console.log('[notify_http response : ' + res.statusCode + ']');

            //response.headers = res.headers;
            if (res.headers['content-type']) {
                response.setHeader('Content-Type', res.headers['content-type']);
            }
            if (res.headers['x-m2m-ri']) {
                response.setHeader('X-M2M-RI', res.headers['x-m2m-ri']);
            }
            if (res.headers['x-m2m-rvi']) {
                response.setHeader('X-M2M-RVI', res.headers['x-m2m-rvi']);
            }
            if (res.headers['x-m2m-rsc']) {
                response.setHeader('X-M2M-RSC', res.headers['x-m2m-rsc']);
            }
            if (res.headers['content-location']) {
                response.setHeader('Content-Location', res.headers['content-location']);
            }

            response.statusCode = res.statusCode;
            response.send(fullBody);
        });
    });

    req.on('error', function (e) {
        console.log('[forward_http] problem with request: ' + e.message);

        response.statusCode = '404';
        response.send(url.parse(request.url).pathname + ' : ' + e.message);
    });

    console.log(request.method + ' - ' + path);
//    console.log(request.headers);
    console.log(request.body);

    // write data to request body
    if ((request.method.toLowerCase() == 'get') || (request.method.toLowerCase() == 'delete')) {
        req.write('');
    }
    else {
        req.write(request.body);
    }
    req.end();
}

function forward_http(forwardcbhost, forwardcbport, request, response) {
    var options = {
        hostname: forwardcbhost,
        port: forwardcbport,
        path: request.url,
        method: request.method,
        headers: request.headers
    };

    var req = http.request(options, function (res) {
        var fullBody = '';
        res.on('data', function (chunk) {
            fullBody += chunk.toString();
        });

        res.on('end', function () {
            console.log('--------------------------------------------------------------------------');
            console.log(res.url);
            console.log(res.headers);
            console.log(fullBody);
            console.log('[Forward response : ' + res.statusCode + ']');

            //response.headers = res.headers;
            if (res.headers['content-type']) {
                response.setHeader('Content-Type', res.headers['content-type']);
            }
            if (res.headers['x-m2m-ri']) {
                response.setHeader('X-M2M-RI', res.headers['x-m2m-ri']);
            }
            if (res.headers['x-m2m-rvi']) {
                response.setHeader('X-M2M-RVI', res.headers['x-m2m-rvi']);
            }
            if (res.headers['x-m2m-rsc']) {
                response.setHeader('X-M2M-RSC', res.headers['x-m2m-rsc']);
            }
            if (res.headers['content-location']) {
                response.setHeader('Content-Location', res.headers['content-location']);
            }

            response.statusCode = res.statusCode;
            response.send(fullBody);
        });
    });

    req.on('error', function (e) {
        console.log('[forward_http] problem with request: ' + e.message);

        response.statusCode = '404';
        response.send(url.parse(request.url).pathname + ' : ' + e.message);
    });

    console.log(request.method + ' - ' + request.url);
    console.log(request.headers);
    console.log(request.body);

    // write data to request body
    if ((request.method.toLowerCase() == 'get') || (request.method.toLowerCase() == 'delete')) {
        req.write('');
    }
    else {
        req.write(request.body);
    }
    req.end();
}

if (process.env.NODE_ENV == 'production') {
    //console.log("Production Mode");
} else if (process.env.NODE_ENV == 'development') {
    //console.log("Development Mode");
}