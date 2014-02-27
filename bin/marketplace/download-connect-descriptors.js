#!/usr/bin/env node

var request = require('request'),
    _ = require('lodash'),
    fs = require('fs'),
    extend = require('node.extend'),
    nomnom = require('nomnom'),
    xml2js = require('xml2js'),
    util = require('util');

var getAddonPage = function (opts) {
    return request({
        uri: opts.baseUrl + opts.uri,
        method: "GET",
        username: opts.user,
        password: opts.pass,
        json: true
    }, function (error, response, body) {
        if (error) {
            console.log("Unable to retrieve marketplace add-ons", response, error);
            return null;
        } else {
            var links = body.links,
                addons = body.plugins;

            var nextRequestLink = _.find(links, {
                    'rel': 'next',
                    'type': 'application/json'
                }),
                nextRequestUri = nextRequestLink ? nextRequestLink.href : null;

            _.forEach(addons, function (addon) {
                if (addon.isOldVersion) {
                    return;
                }

                var name = addon.name,
                    key = addon.pluginKey,
                    version = addon.version;

                if (version.pluginSystemVersion !== 'Three') {
                    return;
                }

                var descriptorUrl = _.find(version.links, { 'rel': 'descriptor' }).href;

                downloadDescriptor(opts, key, descriptorUrl);
            });

            if (!!nextRequestUri) {
                var nextOpts = extend({}, opts);
                nextOpts.uri = nextRequestUri;
                getAddonPage(nextOpts);
            }
        }
    });
};

var downloadDescriptor = function (opts, addonKey, descriptorUrl) {
    request({
        uri: descriptorUrl,
        method: "GET"
    }, function (error, response, body) {
        if (error) {
            console.log("Unable to download descriptor for add-on", addonKey);
        } else { 
            var type = 'xml';
            try {
                JSON.parse(body);
                type = 'json';
            } catch (e) {
            }
            var filename = "descriptors/" + addonKey + '-descriptor' + "." + type;
            fs.writeFile(filename, body, function (err) {
                if (err) {
                    console.log("Unable to write descriptor for add-on " + addonKey + " to disk", err);
                    return;
                }

                if (opts.debug) {
                    console.log(type, '\t', filename);
                }

                if (opts.descriptorDownloadedCallback) {
                    opts.descriptorDownloadedCallback(addonKey, filename, type, body, opts);
                }
            });
        }
    });
}

var getCliOpts = function () {
    return nomnom
       .option('debug', {
          abbr: 'd',
          flag: true,
          help: 'Print debugging info'
       })
       .option('user', {
          abbr: 'u',
          help: 'Marketplace username'
       })
       .option('pass', {
          help: 'Marketplace password'
       })
       .parse();
}

function go(opts) {
    opts = extend(opts, getCliOpts());
    if (!fs.existsSync(opts.downloadDestination)) {
        fs.mkdirSync(opts.downloadDestination);
    }
    getAddonPage(opts);
};

go({
    baseUrl: "https://marketplace.atlassian.com",
    uri: "/rest/1.0/plugins?hosting=ondemand&addOnType=three&includePrivate=true&limit=50",
    downloadDestination: "descriptors/",
    descriptorDownloadedCallback: function (addonKey, filename, type, body, opts) {
        var conditions = [];
        if (type === 'json') {
            var descriptor = JSON.parse(body);
            _.forEach(descriptor.modules, function (module) {
                if (module.conditions) {
                    conditions.push(module.conditions);
                }
            });
        } else {
            xml2js.parseString(body, function (err, descriptor) {
                _.forEach(descriptor['atlassian-plugin'], function (module) {
                    _.forEach(module, function (m) {
                        if (m.conditions) {
                            conditions.push(m.conditions);
                        }
                    })
                });
            });
        }

        if (conditions.length) {
            console.log(addonKey + " : " + util.inspect(conditions, { depth: 5, colors: true }));
        }
    }
});
