#!/usr/bin/env node

var request = require('request'),
    _ = require('lodash'),
    fs = require('fs'),
    extend = require('node.extend'),
    nomnom = require('nomnom')
    util = require('util');

var getAddonPage = function(opts) {
    if (opts.debug) {
        console.log("downloading: " + opts.baseUrl + opts.uri);
        console.log(opts.user, opts.pass);
    }
    return request({
        uri: opts.baseUrl + opts.uri,
        method: "GET",
        username: opts.user,
        password: opts.pass,
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

                downloadDescriptor(opts, key, descriptorUrl);
            });

            console.log("next: " + nextRequestUri);
            if ( !! nextRequestUri) {
                var nextOpts = extend({}, opts);
                nextOpts.uri = nextRequestUri;
                getAddonPage(nextOpts);
            }
        }
    });
};

var downloadDescriptor = function(opts, addonKey, descriptorUrl) {
    request({
        uri: descriptorUrl,
        method: "GET"
    }, function(error, response, body) {
        if (error) {
            console.log("Unable to download descriptor for add-on", addonKey);
        } else {
            var type = 'xml';
            try {
                JSON.parse(body);
                type = 'json';
            } catch (e) {}
            var filename = "descriptors/" + addonKey + '-descriptor' + "." + type;
            fs.writeFile(filename, body, function(err) {
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
        .parse();
}

var defaultOpts = {
    baseUrl: "https://marketplace.atlassian.com",
    uri: "/rest/1.0/plugins?hosting=ondemand&addOnType=three&includePrivate=true&limit=50",
    downloadDestination: "descriptors/"
}

exports.run = function(runOpts) {
    var opts = extend({}, defaultOpts);
    opts = extend(opts, runOpts);
    opts = extend(opts, getCliOpts());

    if (!fs.existsSync(opts.downloadDestination)) {
        fs.mkdirSync(opts.downloadDestination);
    }

    getAddonPage(opts);
};

if (require.main === module) {
    exports.run();
}