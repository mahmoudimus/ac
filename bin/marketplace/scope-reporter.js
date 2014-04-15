#!/usr/bin/env node

var downloader = require('./download-descriptors'),
    _ = require('lodash'),
    util = require('util');

downloader.run({
    descriptorDownloadedCallback: function (result, body, opts) {
        var scopes = [];
        if (result.type === 'json') {
            scopes = JSON.parse(body).scopes;
        } else {
            console.log(result.addon.key, "XML descriptor not supported");
        }

        scopes = _.uniq(_.flatten(scopes));

        if (scopes.length) {
            console.log(result.addon.key + " : " + util.inspect(scopes, { colors: true }));
        }
    }
});

