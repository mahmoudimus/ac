#!/usr/bin/env node

var downloader = require('./download-descriptors'),
    xml2js = require('xml2js'),
    _ = require('lodash'),
    util = require('util');

downloader.run({
    descriptorDownloadedCallback: function (result, body, opts) {
        var conditions = [];
        if (result.type === 'json') {
            var descriptor = JSON.parse(body);
            _.forEach(descriptor.modules, function (m) {
                var c = m.conditions;
                if (c) {
                    c = _.flatten(c, 'condition');
                    conditions.push(c);
                }
            });
        } else {
            xml2js.parseString(body, { mergeAttrs: true }, function (err, descriptor) {
                _.forEach(descriptor['atlassian-plugin'], function (module) {
                    _.forEach(module, function (m) {
                        if (m.conditions) {
                            _.forEach(m.conditions, function (c) {
                                if (c.condition) {
                                    c = c.condition;
                                    c = _.flatten(c, 'name');
                                    conditions.push(c);
                                }
                            });
                        }
                    })
                });
            });
        }

        conditions = _.uniq(_.flatten(conditions));

        if (conditions.length) {
            console.log(result.addon.key + " : " + util.inspect(conditions, { colors: true }));
        }
    }
});

