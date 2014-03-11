#!/usr/bin/env node

var request = require('request'),
    _ = require('lodash'),
    fs = require('fs'),
    extend = require('node.extend'),
    nomnom = require('nomnom'),
    colors = require('colors'),
    util = require('util');

var getAddonPage = function(opts) {
    if (opts.debug) {
        console.log("downloading: " + opts.baseUrl + opts.uri);
    }
    
    return request({
        uri: opts.baseUrl + opts.uri,
        method: "GET",
        auth: opts.auth,
        json: true
    }, function(error, response, body) {
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

            _.forEach(addons, function(addon) {
                if (addon.isOldVersion) {
                    return;
                }

                var name = addon.name,
                    key = addon.pluginKey,
                    version = addon.version;

                if (version.pluginSystemVersion !== 'Three') {
                    return;
                }

                var descriptorUrl = _.find(version.links, {
                    'rel': 'descriptor'
                }).href;

                if (opts.marketplaceAddonCallback) {
                    opts.marketplaceAddonCallback(key, name, version, addon, opts);
                }

                downloadDescriptor(opts, key, addon, descriptorUrl);
            });

            if (nextRequestUri) {
                var nextOpts = extend({}, opts);
                nextOpts.uri = nextRequestUri;
                getAddonPage(nextOpts);
            }
        }
    });
};

var downloadDescriptor = function(opts, addonKey, addon, descriptorUrl) {
    request({
        uri: descriptorUrl,
        method: "GET",
        auth: opts.auth
    }, function(error, response, body) {
        if (error) {
            console.log("Unable to download descriptor for add-on", addonKey);
        } else {
            var type = 'xml',
                typeColor = 'yellow';
            try {
                JSON.parse(body);
                type = 'json';
                typeColor = 'green';
            } catch (e) {}

            if (!opts.type || opts.type === type) {
                var filename = "descriptors/" + addonKey + '-descriptor' + "." + type;
                fs.writeFile(filename, body, function(err) {
                    if (err) {
                        console.log("Unable to write descriptor for add-on " + addonKey + " to disk", err);
                        return;
                    }

                    var status = addon.approval.status.toLowerCase(),
                        statusColor = status === 'public' ? 'blue' : 'red';

                    var url = opts.baseUrl + _.find(addon.links, {
                            'rel': opts.auth ? 'tiny-url' : 'alternate' // no tiny-url for unauthenticated requests?
                        }).href;

                    if (!opts.quiet) {
                        console.log(addonKey, "(" + type[typeColor] + ", " + status[statusColor] + ")", opts.debug ? url.grey : "");
                    }

                    if (opts.descriptorDownloadedCallback) {
                        opts.descriptorDownloadedCallback(addonKey, filename, type, body, opts);
                    }
                });
            } else if (opts.debug) {
                var t = "Ignored add-on " + addonKey + " (" + type + ")";
                console.log(t.grey);
            }
        }
    });
}

var getCliOpts = function() {
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
            abbr: 'p',
            help: 'Marketplace password'
        })
        .option('includePrivate', {
            flag: true,
            help: 'Include private add-ons'
        })
        .option('quiet', {
            abbr: 'q',
            flag: true,
            help: 'Don\'t debug spam'
        })
        .option('type', {
            abbr: 't',
            help: 'Add-on type to filter by. Valid options: [json, xml]',
            choices: ['json', 'xml']
        });
}

var defaultOpts = {
    baseUrl: "https://marketplace.atlassian.com",
    uri: "/rest/1.0/plugins?hosting=ondemand&addOnType=three&limit=50",
    downloadDestination: "descriptors/"
}

exports.run = function(runOpts) {
    var nomnom = getCliOpts();

    var opts = extend({}, defaultOpts);
    opts = extend(opts, runOpts);
    opts = extend(opts, nomnom.parse());

    if (!fs.existsSync(opts.downloadDestination)) {
        fs.mkdirSync(opts.downloadDestination);
    }

    if (opts.includePrivate) {
        opts.uri = opts.uri + "&includePrivate=true";
    }

    if (opts.user && opts.pass) {
        opts.auth = {
            username: opts.user,
            password: opts.pass
        }
    }

    if (!opts.auth) {
        if (!opts.includePrivate) {
            console.log("WARNING: No credentials provided, only public add-ons will be retrieved.");
        } else {
            nomnom.help();
            console.log("ERROR: To retrieve private add-ons, credentials are required.");
            return;
        }
    }

    getAddonPage(opts);
};

if (require.main === module) {
    exports.run();
}