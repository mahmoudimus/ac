#!/usr/bin/env node

var downloader = require('./download-descriptors'),
    _ = require('lodash'),
    util = require('util');

downloader.run({
    descriptorDownloadedCallback: function (result, body, opts) {
        if (result.type === 'json') {
            var descriptor = JSON.parse(body);
            console.log(result.addon.key + " : " + util.inspect(descriptor.baseUrl, { colors: true }));
        } else if (opts.debug) {
            console.log(result.addon.key, "XML descriptor not supported");
        }

    }
});

