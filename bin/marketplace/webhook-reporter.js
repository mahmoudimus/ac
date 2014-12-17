#!/usr/bin/env node

var downloader = require('./download-descriptors'),
    _ = require('lodash'),
    util = require('util');

downloader.run({
    cliOptsCallback: function (nomnom) {
        return nomnom.option('filter', {
            help: 'Webhooks to filter for',
            abbr: 'f',
            list: true
        });
    },
    descriptorDownloadedCallback: function (result, body, opts) {
        var webhooks = [];
        if (result.type === 'json') {
            webhooks = JSON.parse(body).modules.webhooks;
        } else {
            console.log(result.addon.key, "XML descriptor not supported");
        }

        webhooks = _.uniq(_.pluck(webhooks, 'event'));
        if (opts.filter) {
            webhooks = _.intersection(opts.filter, webhooks);
        }

        if (webhooks.length) {
            console.log(result.addon.key + " : " + util.inspect(webhooks, { colors: true }));
        }
    }
});

