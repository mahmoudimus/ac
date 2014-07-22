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
    descriptorDownloadedCallback: function (result, body, opts) {
        if (result.type === 'json') {
            var auth = JSON.parse(body).authentication;
            if (auth) {
                var type = auth.type;
                console.log(result.addon.key + " : " + type[authColor[type]]);
            }
        } else {
            // console.log(result.addon.key, "XML descriptor not supported");
        }
    }
});

