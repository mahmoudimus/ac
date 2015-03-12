#!/usr/bin/env node

var request = require('request'),
    extend = require('node.extend'),
    nomnom = require('nomnom'),
    colors = require('colors'),
    util = require('util');

var selfExecute = require.main === module;

var getUpmToken = function(opts, successCallback, errorCallback) {
    
    return request({
        uri: opts.baseUrl + '/rest/plugins/1.0/',
        method: "HEAD",
        auth: opts.auth
    }, function(error, response, body) {
        if (error || (response.statusCode < 200 || response.statusCode > 299)) {
            console.log("ERROR".red + ": Unable to retrieve upm token.", "Status code: " + ("" + response.statusCode).yellow, error || body || response.body);
            errorCallback && errorCallback(error);
        } else {
            var token = response.headers['upm-token'];
            console.log("token", token);
            successCallback && successCallback(token);
        }
    });
};

var installAddon = function(opts, token) {
    
    return request({
        uri: opts.baseUrl + '/rest/plugins/1.0/?jar=false&token=' + token,
        method: "POST",
        auth: opts.auth,
        body: JSON.stringify({"pluginUri": opts.addonUrl}),
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/vnd.atl.plugins.install.uri+json"
        }
    }, function(error, response, body) {
        if (error || (response.statusCode < 200 || response.statusCode > 299)) {
            console.log("ERROR".red + ": Unable to install add-on", response.statusCode, error || body);
        } else {
            console.log("OK", body);
        }
    });
};

var getCliOpts = function(callback) {
    var nn = nomnom
        .option('debug', {
            abbr: 'd',
            flag: true,
            help: 'Print debugging info'
        })
        .option('user', {
            abbr: 'u',
            help: 'Instance username. Defaults to \'admin\'',
            required: true,
            default: 'admin'
        })
        .option('pass', {
            abbr: 'p',
            help: 'Instance password. Defaults to \'admin\'',
            required: true,
            default: 'admin'
        })
        .option('baseUrl', {
            required: true,
            help: 'Instance base url. Of the form http://localhost:1990/confluence'
        })
        .option('addonUrl', {
            required: true,
            help: 'URL to Connect json descriptor, eg: http://localhost:8000/atlassian-connect.json'
        });

    if (callback) {
        nn = callback(nn);
    }
    return nn;
}

exports.run = function(runOpts) {
    var nomnom = getCliOpts(runOpts ? runOpts.cliOptsCallback : false);

    var opts = extend(opts, runOpts);
    opts = extend(opts, nomnom.parse());

    opts.auth = {
        username: opts.user,
        password: opts.pass
    }

    getUpmToken(opts, function (token) {
        installAddon(opts, token);
    })
};

exports.requestQueue = function () {
    return downloadQueue;
}

if (selfExecute) {
    exports.run();
}
