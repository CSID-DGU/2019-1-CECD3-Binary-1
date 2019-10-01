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
 * @file
 * @copyright KETI Korea 2018, KETI
 * @author Il Yeup Ahn [iyahn@keti.re.kr]
 */

var util = require('util');
var url = require('url');
var http = require('http');
var https = require('https');
var coap = require('coap');
var js2xmlparser = require('js2xmlparser');
var xmlbuilder = require('xmlbuilder');
var fs = require('fs');
var db_sql = require('./sql_action');
var cbor = require("cbor");
var merge = require('merge');

var responder = require('./responder');

var ss_fail_count = {};

var MAX_NUM_RETRY = 16;

function make_xml_noti_message(pc, xm2mri) {
    try {
        var noti_message = {};
        noti_message['m2m:rqp'] = {};
        noti_message['m2m:rqp'].op = 5; // notification
        //noti_message['m2m:rqp'].net = pc['m2m:sgn'].net;
        //noti_message['m2m:rqp'].to = pc['m2m:sgn'].sur;
        noti_message['m2m:rqp'].fr = usecseid;
        noti_message['m2m:rqp'].rqi = xm2mri;
        noti_message['m2m:rqp'].pc = pc;

        if(noti_message['m2m:rqp'].pc.hasOwnProperty('m2m:sgn')) {
            if(noti_message['m2m:rqp'].pc['m2m:sgn'].hasOwnProperty('nev')) {
                for(var prop in noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep) {
                    if (noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep.hasOwnProperty(prop)) {
                        for(var prop2 in noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep[prop]) {
                            if (noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep[prop].hasOwnProperty(prop2)) {
                                if(prop2 == 'rn') {
                                    noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep[prop]['@'] = {rn : noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep[prop][prop2]};
                                    delete noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep[prop][prop2];
                                    break;
                                }
                                else {
                                    for (var prop3 in noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep[prop][prop2]) {
                                        if (noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep[prop][prop2].hasOwnProperty(prop3)) {
                                            if (prop3 == 'rn') {
                                                noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep[prop][prop2]['@'] = {rn: noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep[prop][prop2][prop3]};
                                                delete noti_message['m2m:rqp'].pc['m2m:sgn'].nev.rep[prop][prop2][prop3];
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        noti_message['m2m:rqp']['@'] = {
            "xmlns:m2m": "http://www.onem2m.org/xml/protocols",
            "xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance"
        };

        return js2xmlparser.parse("m2m:rqp", noti_message['m2m:rqp']);
    }
    catch (e) {
        console.log('[make_xml_noti_message] xml parsing error');
        return "";
    }
}

function make_cbor_noti_message(pc, xm2mri) {
    try {
        var noti_message = {};
        noti_message['m2m:rqp'] = {};
        noti_message['m2m:rqp'].op = 5; // notification
        //noti_message['m2m:rqp'].net = pc['m2m:sgn'].net;
        //noti_message['m2m:rqp'].to = pc['m2m:sgn'].sur;
        noti_message['m2m:rqp'].fr = usecseid;
        noti_message['m2m:rqp'].rqi = xm2mri;

        noti_message['m2m:rqp'].pc = pc;

        return cbor.encode(noti_message['m2m:rqp']).toString('hex');
    }
    catch (e) {
        console.log('[make_cbor_noti_message] cbor parsing error');
    }
}

function make_json_noti_message(nu, pc, xm2mri, short_flag) {
    try {
        var noti_message = {};
        noti_message['m2m:rqp'] = {};
        noti_message['m2m:rqp'].op = 5; // notification
        noti_message['m2m:rqp'].rqi = xm2mri;

        if(short_flag == 1) {

        }
        else {
            //noti_message['m2m:rqp'].net = pc['m2m:sgn'].net;
            noti_message['m2m:rqp'].to = nu;
            noti_message['m2m:rqp'].fr = usecseid;
        }

        noti_message['m2m:rqp'].pc = pc;

        return JSON.stringify(noti_message['m2m:rqp']);
    }
    catch (e) {
        console.log('[make_json_noti_message] json parsing error');
    }
}

function sgn_action_send(nu, sub_nu, sub_bodytype, node, short_flag, check_value, ss_cr, ss_ri, xm2mri) {
    if (sub_nu.query != null) {
        var sub_nu_query_arr = sub_nu.query.split('&');
        for (var prop in sub_nu_query_arr) {
            if (sub_nu_query_arr.hasOwnProperty(prop)) {
                if (sub_nu_query_arr[prop].split('=')[0] == 'ct') {
                    if (sub_nu_query_arr[prop].split('=')[1] == 'xml') {
                        sub_bodytype = 'xml';
                    }
                    else {
                        sub_bodytype = 'json';
                    }
                }

                else if (sub_nu_query_arr[prop].split('=')[0] == 'rcn') {
                    if (sub_nu_query_arr[prop].split('=')[1] == '9') {

                        for (var index in node['m2m:sgn'].nev.rep) {
                            if (node['m2m:sgn'].nev.rep.hasOwnProperty(index)) {
                                if (node['m2m:sgn'].nev.rep[index].cr) {
                                    delete node['m2m:sgn'].nev.rep[index].cr;
                                }

                                if (node['m2m:sgn'].nev.rep[index].st) {
                                    delete node['m2m:sgn'].nev.rep[index].st;
                                }

                                delete node['m2m:sgn'].nev.rep[index].ct;
                                delete node['m2m:sgn'].nev.rep[index].lt;
                                delete node['m2m:sgn'].nev.rep[index].et;
                                delete node['m2m:sgn'].nev.rep[index].ri;
                                delete node['m2m:sgn'].nev.rep[index].pi;
                                delete node['m2m:sgn'].nev.rep[index].rn;
                                delete node['m2m:sgn'].nev.rep[index].ty;
                                delete node['m2m:sgn'].nev.rep[index].fr;

                                short_flag = 1;
                            }
                        }
                    }
                }
            }
        }
    }

    if (sub_bodytype == 'xml') {
        if(check_value == 128) {
            node['m2m:sgn'].sud = true;
            delete node['m2m:sgn'].nev;
        }
        else if(check_value == 256) {
            node['m2m:sgn'].vrq = true;
            var temp = node['m2m:sgn'].sur;
            delete node['m2m:sgn'].sur;
            node['m2m:sgn'].sur = temp;
            node['m2m:sgn'].cr = ss_cr;
            delete node['m2m:sgn'].nev;
        }
        node['m2m:sgn'].rvi = uservi;

        if (sub_nu.protocol == 'http:') {
            try {
                var bodyString = responder.convertXmlSgn(Object.keys(node)[0], node[Object.keys(node)[0]]);

            }
            catch (e) {
                bodyString = "";
            }

            if (bodyString == "") { // parse error
                ss_fail_count[ss_ri]++;
                console.log('can not send notification since error of converting json to xml');
            }
            else {
                request_noti_http(nu, ss_ri, bodyString, sub_bodytype, xm2mri);
            }
        }
        else if (sub_nu.protocol == 'coap:') {
            try {
                bodyString = responder.convertXmlSgn(Object.keys(node)[0], node[Object.keys(node)[0]]);
            }
            catch (e) {
                bodyString = "";
            }

            if (bodyString == "") { // parse error
                ss_fail_count[ss_ri]++;
                console.log('can not send notification since error of converting json to xml');
            }
            else {
                request_noti_coap(nu, ss_ri, bodyString, sub_bodytype, xm2mri);
            }
        }
        else if (sub_nu.protocol == 'ws:') {
            // if(check_value == 128) {
            //     node['m2m:sgn'].sud = true;
            //     delete node['m2m:sgn'].nev;
            // }
            // else if(check_value == 256) {
            //     node['m2m:sgn'].vrq = true;
            //     temp = node['m2m:sgn'].sur;
            //     delete node['m2m:sgn'].sur;
            //     node['m2m:sgn'].sur = temp;
            //     node['m2m:sgn'].cr = ss_cr;
            //     delete node['m2m:sgn'].nev;
            // }

            bodyString = make_xml_noti_message(node, xm2mri);
            //bodyString = responder.convertXmlSgn(Object.keys(node)[0], node[Object.keys(node)[0]]);
            if (bodyString == "") { // parse error
                ss_fail_count[ss_ri]++;
                console.log('can not send notification since error of converting json to xml');
            }
            else {
                request_noti_ws(nu, ss_ri, bodyString, sub_bodytype, xm2mri);
            }
        }
        else { // mqtt:
            bodyString = make_xml_noti_message(node, xm2mri);
            //bodyString = responder.convertXmlSgn(Object.keys(node)[0], node[Object.keys(node)[0]]);
            if (bodyString == "") { // parse error
                ss_fail_count[ss_ri]++;
                console.log('can not send notification since error of converting json to xml');
            }
            else {
                request_noti_mqtt(nu, ss_ri, bodyString, sub_bodytype, xm2mri);
            }
        }
    }
    else if (sub_bodytype == 'cbor') {
        if(check_value == 128) {
            node['m2m:sgn'].sud = true;
            delete node['m2m:sgn'].nev;
        }
        else if(check_value == 256) {
            node['m2m:sgn'].vrq = true;
            temp = node['m2m:sgn'].sur;
            delete node['m2m:sgn'].sur;
            node['m2m:sgn'].sur = temp;
            node['m2m:sgn'].cr = ss_cr;
            delete node['m2m:sgn'].nev;
        }
        node['m2m:sgn'].rvi = uservi;

        if (sub_nu.protocol == 'http:') {
            //node['m2m:'+Object.keys(node)[0]] = node[Object.keys(node)[0]];
            //delete node[Object.keys(node)[0]];
            bodyString = cbor.encode(node).toString('hex');
            request_noti_http(nu, ss_ri, bodyString, sub_bodytype, xm2mri);
        }
        else if (sub_nu.protocol == 'coap:') {
            //node['m2m:'+Object.keys(node)[0]] = node[Object.keys(node)[0]];
            //delete node[Object.keys(node)[0]];
            bodyString = cbor.encode(node).toString('hex');
            request_noti_coap(nu, ss_ri, bodyString, sub_bodytype, xm2mri);
        }
        else if (sub_nu.protocol == 'ws:') {
            bodyString = make_cbor_noti_message(node, xm2mri);
            request_noti_ws(nu, ss_ri, bodyString, sub_bodytype, xm2mri);
        }
        else { // mqtt:
            bodyString = make_cbor_noti_message(node, xm2mri);
            request_noti_mqtt(nu, ss_ri, bodyString, sub_bodytype, xm2mri);
        }
    }
    else { // defaultbodytype == 'json')
        if(check_value == 128) {
            node['m2m:sgn'].sud = true;
            delete node['m2m:sgn'].nev;
        }
        else if(check_value == 256) {
            node['m2m:sgn'].vrq = true;
            temp = node['m2m:sgn'].sur;
            delete node['m2m:sgn'].sur;
            node['m2m:sgn'].sur = temp;
            node['m2m:sgn'].cr = ss_cr;
            delete node['m2m:sgn'].nev;
        }
        node['m2m:sgn'].rvi = uservi;

        if (sub_nu.protocol == 'http:') {
            //node['m2m:'+Object.keys(node)[0]] = node[Object.keys(node)[0]];
            //delete node[Object.keys(node)[0]];
            request_noti_http(nu, ss_ri, JSON.stringify(node), sub_bodytype, xm2mri);
        }
        else if (sub_nu.protocol == 'coap:') {
            //node['m2m:'+Object.keys(node)[0]] = node[Object.keys(node)[0]];
            //delete node[Object.keys(node)[0]];
            request_noti_coap(nu, ss_ri, JSON.stringify(node), sub_bodytype, xm2mri);
        }
        else if (sub_nu.protocol == 'ws:') {
            bodyString = make_json_noti_message(nu, node, xm2mri, short_flag);
            request_noti_ws(nu, ss_ri, bodyString, sub_bodytype, xm2mri);
        }
        else { // mqtt:
            bodyString = make_json_noti_message(nu, node, xm2mri, short_flag);
            request_noti_mqtt(nu, ss_ri, bodyString, sub_bodytype, xm2mri);
        }
    }
}

function sgn_action(rootnm, check_value, results_ss, noti_Obj, sub_bodytype) {
    var nct = results_ss.nct;
    var enc_Obj = results_ss.enc;
    var net_arr = enc_Obj.net;

    for (var j = 0; j < net_arr.length; j++) {
        /* for testing, make comment statement
        if (net_arr[j] == check_value) { // 1 : Update_of_Subscribed_Resource, 3 : Create_of_Direct_Child_Resource, 4 : Delete_of_Direct_Child_Resource
         */
        if (net_arr[j] == check_value || check_value == 256 || check_value == 128) { // 1 : Update_of_Subscribed_Resource, 3 : Create_of_Direct_Child_Resource, 4 : Delete_of_Direct_Child_Resource
            var nu_arr = results_ss.nu;
            for (var k = 0; k < nu_arr.length; k++) {
                var nu = nu_arr[k];

                var node = {};
                node['m2m:sgn'] = {};

                if(results_ss.ri.charAt(0) == '/') {
                    node['m2m:sgn'].sur = results_ss.ri.replace('/', '');
                }
                else {
                    node['m2m:sgn'].sur = results_ss.ri;
                }

                if (results_ss.nec) {
                    node['m2m:sgn'].nec = results_ss.nec;
                }
                node['m2m:sgn'].nev = {};
                node['m2m:sgn'].nev.net = parseInt(net_arr[j].toString());
                node['m2m:sgn'].nev.rep = {};
                node['m2m:sgn'].nev.rep['m2m:' + rootnm] = noti_Obj;

                responder.typeCheckforJson(node['m2m:sgn'].nev.rep);

                var xm2mri = require('shortid').generate();
                var short_flag = 0;

                var sub_nu = url.parse(nu);

                if(sub_nu.protocol == null) { // ID format
                    var absolute_url = nu;
                    absolute_url = absolute_url.replace(usespid + usecseid + '/', '/');
                    absolute_url = absolute_url.replace(usecseid + '/', '/');

                    if(absolute_url.charAt(0) != '/') {
                        absolute_url = '/' + absolute_url;
                    }

                    var absolute_url_arr = absolute_url.split('/');

                    db_sql.get_ri_sri(node, absolute_url, absolute_url_arr[1].split('?')[0], function (err, results, node, absolute_url) {
                        if (err) {
                            console.log('[sgn_action] database error (can not get resourceID from database)');
                        }
                        else {
                            absolute_url = (results.length == 0) ? absolute_url : ((results[0].hasOwnProperty('ri')) ? absolute_url.replace('/' + absolute_url_arr[1], results[0].ri) : absolute_url);

                            db_sql.select_direct_lookup(absolute_url, function (err, comm_Obj) {
                                if(!err) {
                                    if(comm_Obj.length == 1) {
                                        if(comm_Obj[0].ty == 2) {
                                            db_sql.select_resource(responder.typeRsrc[comm_Obj[0].ty], comm_Obj[0].ri, function (err, spec_Obj) {
                                                if (!err) {
                                                    if (spec_Obj.length == 1) {
                                                        if (spec_Obj[0].poa != null || spec_Obj[0].poa != '') {
                                                            var poa_arr = JSON.parse(spec_Obj[0].poa);
                                                            for (var i = 0; i < poa_arr.length; i++) {
                                                                sub_nu = url.parse(poa_arr[i]);
                                                                if(sub_nu.protocol == null) {
                                                                    nu = 'http://localhost:7579' + absolute_url;
                                                                    sub_nu = url.parse(nu);
                                                                    if (nct == 2 || nct == 1) {
                                                                        sgn_action_send(nu, sub_nu, sub_bodytype, node, short_flag, check_value, results_ss.cr, results_ss.ri, xm2mri);
                                                                    }
                                                                }
                                                                else {
                                                                    nu = poa_arr[i];
                                                                    if (nct == 2 || nct == 1) {
                                                                        sgn_action_send(nu, sub_nu, sub_bodytype, node, short_flag, check_value, results_ss.cr, results_ss.ri, xm2mri);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        else {
                                            console.log('[sgn_action] nu resource is not AE resource)');
                                        }
                                    }
                                    else {
                                        console.log('[sgn_action] nu resource is not exist)');
                                    }
                                }
                                else {
                                    console.log('[sgn_action] database error (nu resource)');
                                }
                            });
                        }
                    });
                }
                else { // url format
                    if (nct == 2 || nct == 1) {
                        sgn_action_send(nu, sub_nu, sub_bodytype, node, short_flag, check_value, results_ss.cr, results_ss.ri, xm2mri);
                    }
                    else {
                        console.log('nct except 2 (All Attribute) do not support');
                    }
                }
            }
        }
        //else {
        //    console.log('enc-net except 3 do not support');
        //}
    }
}

exports.check = function(request, notiObj, check_value) {
    var rootnm = request.headers.rootnm;

    if((request.method == "PUT" && check_value == 1)) {
        var pi = notiObj.ri;
    }
    else if ((request.method == "POST" && check_value == 3) || (request.method == "DELETE" && check_value == 4)) {
        pi = notiObj.pi;
    }

    var ri = notiObj.ri;

    var noti_Str = JSON.stringify(notiObj);
    var noti_Obj = JSON.parse(noti_Str);

    if (request.query.real == 4) {
        // for test of measuring elapsed time of processing in mobius
        // var hrend = process.hrtime(elapsed_hrstart[request.headers.elapsed_tid]);
        // var elapsed_hr_str = util.format(require('moment')().utc().format('YYYYMMDDTHHmmss') + "(hr): %ds %dms\r\n", hrend[0], hrend[1]/1000000);
        // console.info(elapsed_hr_str);
        // console.timeEnd(request.headers.elapsed_tid);
        // var fs = require('fs');
        // fs.appendFileSync('get_elapsed_time.log', elapsed_hr_str, 'utf-8');
        // delete elapsed_hrstart[request.headers.elapsed_tid];
        if(request.query.hasOwnProperty('nu')) {
            var results_ss = {};
            results_ss.ri = pi + '/' + (request.query.hasOwnProperty('rn') ? request.query.rn : 'sub');
            results_ss.nct = '2';
            results_ss.enc = {};
            results_ss.enc.net = [];
            results_ss.enc.net.push('3');
            results_ss.nu = [];
            results_ss.nu.push((request.query.hasOwnProperty('nu') ? request.query.nu : 'http://localhost'));
            //if (ss_fail_count[results_ss.ri] == null) {
                ss_fail_count[results_ss.ri] = 0;
            //}
            sgn_action(rootnm, check_value, results_ss, noti_Obj, request.headers.usebodytype);
        }
        return'1';
    }

    if(check_value == 256 || check_value == 128) { // verification
        ss_fail_count[ri] = 0;
        sgn_action(rootnm, check_value, notiObj, noti_Obj, request.headers.usebodytype);
    }
    else {
        var noti_ri = noti_Obj.ri;
        noti_Obj.ri = noti_Obj.sri;
        delete noti_Obj.sri;
        noti_Obj.pi = noti_Obj.spi;
        delete noti_Obj.spi;

        db_sql.select_sub(pi, function (err, results_ss) {
            if (!err) {
                for (var i = 0; i < results_ss.length; i++) {
                    if(results_ss[i].ri == noti_ri) {
                        continue;
                    }
                    for (var index in results_ss[i]) {
                        if (results_ss[i].hasOwnProperty(index)) {
                            if (request.hash) {
                                if (request.hash.split('#')[1] == index) {

                                }
                                else {
                                    delete results_ss[i][index];
                                }
                            }
                            else {
                                if(index == 'enc' || index == 'nu') {
                                    results_ss[i][index] = JSON.parse(results_ss[i][index]);
                                }

                                if (typeof results_ss[i][index] === 'boolean') {
                                    results_ss[i][index] = results_ss[i][index].toString();
                                }
                                else if (typeof results_ss[i][index] === 'string') {
                                    if (results_ss[i][index] == '' || results_ss[i][index] == 'undefined' || results_ss[i][index] == '[]') {
                                        if (results_ss[i][index] == '' && index == 'pi') {
                                            results_ss[i][index] = 'NULL';
                                        }
                                        else {
                                            delete results_ss[i][index];
                                        }
                                    }
                                }
                                else if (typeof results_ss[i][index] === 'number') {
                                    results_ss[i][index] = results_ss[i][index].toString();
                                }
                                else {
                                }
                            }
                        }
                    }

                    var xm2mri = require('shortid').generate();
                    if (ss_fail_count[results_ss[i].ri] == null) {
                        ss_fail_count[results_ss[i].ri] = 0;
                    }
                    sgn_action(rootnm, check_value, results_ss[i], noti_Obj, request.headers.usebodytype);
                }
            }
            else {
                console.log('query error: ' + results_ss.message);
            }
        });
    }
};


function request_noti_http(nu, ri, bodyString, bodytype, xm2mri) {
    if(ss_fail_count[ri] == null) {
        ss_fail_count[ri] = 0;
    }

    ss_fail_count[ri]++;
    if (ss_fail_count[ri] >= MAX_NUM_RETRY) {
        delete ss_fail_count[ri];
        delete_sub(ri, xm2mri);
        console.log('      [request_noti_http - ' + ss_fail_count[ri] + '] remove subscription because no response');
    }
    else {
        var bodyStr = '';
        var options = {
            hostname: url.parse(nu).hostname,
            port: url.parse(nu).port,
            path: url.parse(nu).path,
            method: 'POST',
            headers: {
                'X-M2M-RI': xm2mri,
                'Accept': 'application/' + bodytype,
                'X-M2M-Origin': usecseid,
                'Content-Type': 'application/' + bodytype,
                'Content-Length' : bodyString.length,
                'ri': ri,
                'X-M2M-RVI': uservi
            }
        };

        function response_noti_http(res) {
            res.on('data', function (chunk) {
                bodyStr += chunk;
            });

            res.on('end', function () {
                if (res.statusCode == 200 || res.statusCode == 201) {
                    console.log('----> [request_noti_http - ' + ss_fail_count[res.req._headers.ri] + ']');
                    delete ss_fail_count[res.req._headers.ri];
                }
            });
        }

        if (url.parse(nu).protocol == 'http:') {
            var req = http.request(options, function (res) {
                response_noti_http(res);
            });
        }
        else {
            options.ca = fs.readFileSync('ca-crt.pem');

            req = https.request(options, function (res) {
                response_noti_http(res);
            });
        }

        req.on('error', function (e) {
            console.log('[request_noti_http - problem with request: ' + e.message + ']');
            console.log('[request_noti_http - no response - ' + ss_fail_count[ri] + ']');
            // if (ss_fail_count[req._headers.ri] >= MAX_NUM_RETRY) {
            //     delete ss_fail_count[req._headers.ri];
            //     delete_sub(req._headers.ri, xm2mri);
            // }
        });

        req.on('close', function () {
            console.log('[request_noti_http - close: no response for notification');
        });

        console.log('<---- [request_noti_http - ' + ss_fail_count[ri] + '] ');
        console.log(bodyString);
        req.write(bodyString);
        req.end();
    }
}


function request_noti_coap(nu, ri, bodyString, bodytype, xm2mri) {
    var options = {
        host: url.parse(nu).hostname,
        port: url.parse(nu).port,
        pathname: url.parse(nu).path,
        method: 'post',
        confirmable: 'false',
        options: {
            'Accept': 'application/'+bodytype,
            'Content-Type': 'application/'+bodytype,
            'Content-Length' : bodyString.length
        }
    };

    var responseBody = '';
    var req = coap.request(options);
    req.setOption("256", new Buffer(usecseid));      // X-M2M-Origin
    req.setOption("257", new Buffer(xm2mri));    // X-M2M-RI
    req.on('response', function (res) {
        res.on('data', function () {
            responseBody += res.payload.toString();
        });

        res.on('end', function () {
            if(res.code == '2.03' || res.code == '2.01') {
                ss_fail_count[ri] = 0;
                console.log('----> [request_noti_coap] response for notification through coap  ' + res.code + ' - ' + ri);
            }
        });
    });

    req.on('error', function (e) {
        console.log('[request_noti_coap] problem with request: ' + e.message);
    });

    console.log('<---- [request_noti_coap] request for notification through coap with ' + bodytype);
    console.log(bodyString);
    req.write(bodyString);
    req.end();
}

exports.response_noti_handler = function(topic, message) {
    var topic_arr = topic.split('/');
    if(topic_arr[5] != null) {
        var bodytype = (topic_arr[5] == 'xml') ? topic_arr[5] : ((topic_arr[5] == 'json') ? topic_arr[5] : ((topic_arr[5] == 'cbor') ? topic_arr[5] : 'json'));
    }
    else {
        bodytype = defaultbodytype;
        topic_arr[5] = defaultbodytype;
    }

    if((topic_arr[1] == 'oneM2M' && topic_arr[2] == 'resp' && ((topic_arr[3].replace(':', '/') == usecseid) || (topic_arr[3] == usecseid.replace('/', ''))))) {
        make_json_obj(bodytype, message.toString(), function(rsc, jsonObj) {
            if(rsc == '1') {
                if(jsonObj['m2m:rsp'] == null) {
                    jsonObj['m2m:rsp'] = jsonObj;
                }

                var ss_ri_cache = get_all_ss_ri_cache();
                for (var idx in ss_ri_cache) {
                    if (ss_ri_cache.hasOwnProperty(idx)) {
                        if(idx == jsonObj['m2m:rsp'].rqi) {
                            var ri = ss_ri_cache[idx].ri;

                            noti_mqtt.unsubscribe(topic);

                            console.log('----> [response_noti_mqtt - ' + ss_fail_count[ri] + '] ' + jsonObj['m2m:rsp'].rsc + ' - ' + topic);
                            NOPRINT === 'true' ? NOPRINT = 'true' : console.log(message.toString());

                            ss_fail_count[ri] = 0;
                            delete ss_fail_count[ri];
                            delete ss_ri_cache[idx];

                            del_ss_ri_cache(idx);
                        }
                    }
                }
            }
            else {
                console.log('[response_noti_mqtt] parsing error');
            }
        });
    }
};

var cache_ttl = 2;
function request_noti_mqtt(nu, ri, bodyString, bodytype, xm2mri) {
    if(noti_mqtt == null) {
        console.log('[request_noti_mqtt] noti_mqtt is not connected to Mobius');
        return '0';
    }

    try {
        var aeid = url.parse(nu).pathname.replace('/', '').split('?')[0];
        var noti_resp_topic = '/oneM2M/resp/' + usecseid.replace('/', '') + '/' + aeid + '/' + bodytype;
        var noti_resp_topic2 = '/oneM2M/resp/' + usecsebase + '/' + aeid + '/' + bodytype;

        if(ss_fail_count[ri] == null) {
            ss_fail_count[ri] = 0;
        }
        ss_fail_count[ri]++;
        if (ss_fail_count[ri] >= MAX_NUM_RETRY) {
            delete ss_fail_count[ri];
            delete_sub(ri, xm2mri);
            noti_mqtt.unsubscribe(noti_resp_topic);
            console.log('      [request_noti_mqtt - ' + ss_fail_count[ri] + '] remove subscription because no response');
        }
        else {
            var ss_ri_cache = get_all_ss_ri_cache();
            ss_ri_cache[xm2mri] = {};
            ss_ri_cache[xm2mri].ri = ri;
            ss_ri_cache[xm2mri].ttl = cache_ttl;

            set_ss_ri_cache(xm2mri, ss_ri_cache[xm2mri]);

            //noti_mqtt.unsubscribe(noti_resp_topic);
            noti_mqtt.subscribe(noti_resp_topic);
            console.log('subscribe noti_resp_topic as ' + noti_resp_topic);

            noti_mqtt.subscribe(noti_resp_topic2);
            console.log('subscribe noti_resp_topic as ' + noti_resp_topic2);

            var noti_topic = '/oneM2M/req/' + usecseid.replace('/', '') + '/' + aeid + '/' + bodytype;
            noti_mqtt.publish(noti_topic, bodyString);
            console.log('<---- [request_noti_mqtt - ' + ss_fail_count[ri] + '] publish - ' + noti_topic);
            NOPRINT==='true'?NOPRINT='true':console.log(bodyString);
        }
    }
    catch (e) {
        console.log(e.message);
        console.log('can not send notification to ' + nu);
    }
}

function request_noti_ws(nu, ri, bodyString, bodytype, xm2mri) {
    var bodyStr = '';

    if(usesecure == 'disable') {
        var WebSocketClient = require('websocket').client;
        var ws_client = new WebSocketClient();

        if(bodytype == 'xml') {
            ws_client.connect(nu, 'onem2m.r2.0.xml');
        }
        else if(bodytype == 'cbor') {
            ws_client.connect(nu, 'onem2m.r2.0.cbor');
        }
        else {
            ws_client.connect(nu, 'onem2m.r2.0.json');
        }

        ws_client.on('connectFailed', function (error) {
            ss_fail_count[ri]++;
            console.log('Connect Error: ' + error.toString() + ' - ' + ss_fail_count[ri]);
            ws_client.removeAllListeners();

            if (ss_fail_count[ri] >= MAX_NUM_RETRY) {
                delete ss_fail_count[ri];
                delete_sub(ri, xm2mri);
            }
        });

        ws_client.on('connect', function (connection) {
            console.log('<---- [request_noti_ws] ' + nu + ' - ' + bodyString);

            connection.sendUTF(bodyString);

            connection.on('error', function (error) {
                console.log("[request_noti_ws] Connection Error: " + error.toString());

            });
            connection.on('close', function () {
                console.log('[request_noti_ws] Connection Closed');
            });
            connection.on('message', function (message) {
                console.log(message.utf8Data.toString());

                console.log('----> [request_noti_ws] ' + message.utf8Data.toString());
                ss_fail_count[ri] = 0;

                connection.close();
            });
        });
    }
    else {
        console.log('not support secure notification through ws');
    }
}

function delete_sub(ri, xm2mri) {
    // db_sql.delete_ri_lookup(ri, function (err) {
    //     if (!err) {
    //         console.log('delete sgn of ' + ri + ' for no response');
    //     }
    // });

    var bodyStr = '';
    var options = {
        hostname: 'localhost',
        port: usecsebaseport,
        path: ri,
        method: 'DELETE',
        headers: {
            'X-M2M-RI': xm2mri,
            'Accept': 'application/json',
            'X-M2M-Origin': usesuperuser,
            'X-M2M-RVI': uservi
        }
    };

    function response_del_sub(res) {
        res.on('data', function (chunk) {
            bodyStr += chunk;
        });

        res.on('end', function () {
            if (res.statusCode == 200 || res.statusCode == 202) {
                console.log('----> [delete_sub - ' + res.statusCode + ']');
            }
        });
    }

    if(usesecure == 'disable') {
        var req = http.request(options, function (res) {
            response_del_sub(res);
        });
    }
    else {
        options.ca = fs.readFileSync('ca-crt.pem');
        req = https.request(options, function (res) {
            response_del_sub(res);
        });
    }

    req.on('error', function (e) {
        console.log('[delete_sub - problem with request: ' + e.message + ']');
    });

    req.on('close', function() {
        console.log('[delete_sub - close: no response for notification');
    });

    console.log('<---- [delete_sub - ]');
    req.write('');
    req.end();
}

