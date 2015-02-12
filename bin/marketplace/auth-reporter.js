#!/usr/bin/env node

var downloader = require('./download-descriptors'),
    _ = require('lodash'),
    util = require('util'),
    c = require('colors');

var authColor = {
    "none": "yellow",
    "oauth": "red",
    "jwt": "blue"
}

downloader.run({
    cliOptsCallback: function (nomnom) {
        return nomnom.option('filter', {
            help: 'Auth type to filter for',
            abbr: 'f',
            list: true
        });
    },
    descriptorDownloadedCallback: function (result, body, opts) {
        if (result.type === 'json') {
            var auth = JSON.parse(body).authentication;
            var type = (auth && auth.type) ? auth.type : 'none';
            // console.log(result.addon.key, util.inspect(auth));
            var color = authColor[type];

            if (opts.filter && !_.contains(opts.filter, type)) {
                return;
            }
            
            console.log(type[color] + ' : ' + result.addon.key);
        } else {
            // console.log(result.addon.key, "XML descriptor not supported");
        }
    }
});

