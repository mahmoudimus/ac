#!/usr/bin/env node

var downloader = require('./download-descriptors'),
    xml2js = require('xml2js'),
    _ = require('lodash'),
    util = require('util');

var ignoredXmlElements = [
    '$',
    'plugin-info',
    'remote-plugin-container'
];

var counter = 0,
    report = [];


function finished() {
    var mods = _.flatten(_.pluck(report, 'modules'));

    var result = {};
    _.forEach(mods, function (module) {
        var key = module.key,
            count = module.count,
            id = module.id,
            m;

        if (!result[key]) {
            result[key] = m = {
                count: 0
            };
        } else {
            m = result[key];
        }

        m.count += count;
        if (id) {
            if (!m.ids || !m.ids[id]) {
                if (!m.ids) {
                    m.ids = {};
                }
                m.ids[id] = {
                    count: 1
                };
            } else {
                m.ids[id].count++;
            }
        }
    });

    console.log(util.inspect(result, {colors: true, depth: 10}));
}

downloader.run({
    preDescriptorDownloadedCallback: function(data, opts) {
        counter++;
    },
    descriptorDownloadedCallback: function (data, body, opts) {
        var modules = [];
        try {
            if (data.type === 'json') {
                _.forEach(JSON.parse(body).modules, function (values, key) {
                    if (!_.isArray(values)) {
                        values = [values];
                    }
                    _.forEach(values, function (value) {
                        var id;
                        
                        if (value.location) {
                            id = value.location;
                        }
                        if (value.event) {
                            id = value.event;
                        }
                        var m = {
                            key: key,
                            count: 1
                        }
                        if (id) {
                            m.id = id;
                        }
                        modules.push(m);
                    });
                });
            } else {
                xml2js.parseString(body, {
                    normalizeTags: true
                }, function (err, descriptor) {
                    _.forEach(descriptor['atlassian-plugin'], function (values, key) {
                        if (_.contains(ignoredXmlElements, key)) {
                            return;
                        }
                        _.forEach(values, function (value) {                        
                            var attrs = value['$'],
                                id;
                            if (attrs) {
                                if (attrs.location) {
                                    id = attrs.location;
                                }
                                if (attrs.section) {
                                    id = attrs.section;
                                }
                                if (attrs.event) {
                                    id = attrs.event;
                                }
                            }
                            var m = {
                                key: key,
                                count: 1
                            }
                            if (id) { 
                                m.id = id;
                            }
                            modules.push(m);
                        });
                    });
                });
            }
        } catch (e) {
            console.log("ERROR".red, data.addon.key, e);
        }

        counter--;
        if (counter <= 0) {
            finished();
        }

        report.push({
            key: data.addon.key,
            modules: modules
        });
    }
});

