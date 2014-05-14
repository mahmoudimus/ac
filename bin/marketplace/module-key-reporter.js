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

    mods = _.uniq(_.flatten(mods));

    if (mods.length) {
        console.log(util.inspect(mods, { colors: true }));
    }
    
//    _.forEach(mods, function (module) {
//        var key = module.key,
//            count = module.count,
//            id = module.id,
//            m;
//
//        if (!result[key]) {
//            result[key] = m = {
//                count: 0
//            };
//        } else {
//            m = result[key];
//        }
//
//        m.count += count;
//        if (id) {
//            if (!m.ids || !m.ids[id]) {
//                if (!m.ids) {
//                    m.ids = {};
//                }
//                m.ids[id] = {
//                    count: 1
//                };
//            } else {
//                m.ids[id].count++;
//            }
//        }
//    });
//
//    console.log(util.inspect(result, {colors: true, depth: 10}));
}

downloader.run({
    preDescriptorDownloadedCallback: function(data, opts) {
        counter++;
    },
    descriptorDownloadedCallback: function (data, body, opts) {
        var modules = [];
        try {
            if (data.type === 'json') {
                var jsonBody = JSON.parse(body);
                _.forEach(jsonBody.modules, function (values, key) {
                    if (!_.isArray(values)) {
                        values = [values];
                    }
                    _.forEach(values, function (value) {
                        var m = {
                            addon: jsonBody.key,
                            key: value.key,
                            valid: /^[a-zA-Z0-9-]+$/.test(value.key)
                        }
                        modules.push(m);
                    });
                });
            } else {
                //we don't care about xml
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

