#!/usr/bin/env node

var downloader = require('./download-descriptors'),
    _ = require('lodash'),
    URI = require('URIjs'),
    util = require('util');

downloader.run({
    descriptorDownloadedCallback: function (result, body, opts) {
        if (result.type === 'json') {
            var descriptor = JSON.parse(body);
            var auth = (descriptor.authentication && descriptor.authentication.type) || "none";
            var uri = URI(descriptor.baseUrl);
            if (auth === 'jwt' && uri.path().length > 1) {
                console.log(result.addon.key, auth, descriptor.baseUrl.red); 
            } else {
                console.log(result.addon.key.grey, auth.grey, descriptor.baseUrl.grey); 
            }
        } else if (opts.debug) {
            console.log(result.addon.key, "XML descriptor not supported");
        }

    }
});

