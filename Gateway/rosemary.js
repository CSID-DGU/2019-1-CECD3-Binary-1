/**
 * Copyright (c) 2018, OCEAN
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Created by Il Yeup, Ahn in KETI on 2016-07-28.
 * Updated on 2018-05-29
 */

var mn = require('./mobius/mn');
var fs = require('fs');

var data  = fs.readFileSync('conf_mn.json', 'utf-8');
var conf = JSON.parse(data);


global.defaultnmtype        = 'short';
global.defaultbodytype      = 'json';


// parent CSE information
global.parent_cbname        = conf.parent.cbname;
global.parent_cbcseid       = conf.parent.cbcseid;
global.parent_cbhost        = conf.parent.cbhost;
global.parent_cbhostport    = conf.parent.cbhostport;
global.parent_cbprotocol    = conf.parent.cbprotocol;       // select 'http' or 'mqtt' when register remoteCSE
global.parent_mqttbroker    = conf.parent.mqttbroker;


// my CSE information
global.usecsetype           = 'mn'; // select 'in' or 'mn' or 'asn'
global.usecsebase           = 'rosemary';
global.usecseid             = '/rosemary2';
global.usecsebaseport       = conf.csebaseport;

global.usedbhost            = 'localhost';
global.usedbpass            = conf.dbpass;


global.usepxywsport         = '7573';
global.usepxymqttport       = '7574';


global.usetsagentport       = '7572';

global.usemqttbroker        = parent_mqttbroker; // mobius to mqttbroker
global.usesecure            = 'disable';
if(usesecure === 'enable') {
    global.usemqttport      = '8883';
}
else {
    usemqttport             = '1883';
}

global.useaccesscontrolpolicy = 'disable';

global.wdt = require('./wdt');

global.allowed_ae_ids = [];
//allowed_ae_ids.push('ryeubi');

global.allowed_app_ids = [];
//allowed_app_ids.push('APP01');

global.usesemanticbroker    = '10.10.202.114';

global.uservi = '2a';

// CSE core
require('./app');

var events = require('events');
global.csr_custom = new events.EventEmitter();

csr_custom.on('register_remoteCSE', function() {
    mn.build_mn('/'+usecsebase, function (rsp) {
        if(rsp.rsc == '2000') {
            console.log('[[[[[[[[[[[[[[[[register_remoteCSE]]]]]]]]]]]]]]]] ' + JSON.stringify(rsp));
            clearInterval(refreshIntervalId);
        }
        else {
            console.log('register_remoteCSE again');
        }
    });
});



