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
        console.log("downloading: ".grey + opts.baseUrl + opts.uri);
    }
    
    return request({
        uri: opts.baseUrl + opts.uri,
        method: "GET",
        auth: opts.auth,
        json: true
    }, function(error, response, body) {
        if (error) {
            console.log("ERROR".red + ": Unable to retrieve marketplace add-ons", response, error);
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
                var version = addon.version,
                    deployment = version.deployment,
                    releaseDate = version.releaseDate || "(unknown date)";
                if (!deployment || !deployment.remote) {
                    console.log("No deployment info for " + addon.pluginKey.red);
                    return false;
                }

                var type = deployment.descriptorType,
                    typeColor = type === 'json' ? 'green' : 'yellow',
                    status = addon.approval.status.toLowerCase(),
                    statusColor = status === 'public' ? 'blue' : 'red',
                    url = opts.baseUrl + _.find(addon.links, {
                        'rel': opts.auth ? 'tiny-url' : 'alternate' // no tiny-url for unauthenticated requests?
                    }).href;

                if (opts.type && opts.type !== type) {
                    return;
                }

                if (!opts.quiet) {
                    try {
                        console.log(addon.pluginKey, "(" + type[typeColor] + ", " + status[statusColor] + ")", opts.debug ? (releaseDate + " " + url.grey) : "");
                    } catch (e) {
                        if (opts.debug) {
                            console.log(("" + e).red);
                            console.log(JSON.stringify(addon));
                        }
                        process.exit();
                    }
                }

                if (opts.marketplaceAddonCallback) {
                    opts.marketplaceAddonCallback(addon, opts);
                }
            });

            if (nextRequestUri) {
                var nextOpts = extend({}, opts);
                nextOpts.uri = nextRequestUri;
                getAddonPage(nextOpts);
            }
        }
    });
};



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
    uri: "/rest/1.0/plugins?hosting=ondemand&addOnType=three&limit=50"
}

exports.run = function(runOpts) {
    var nomnom = getCliOpts();

    var opts = extend({}, defaultOpts);
    opts = extend(opts, runOpts);
    opts = extend(opts, nomnom.parse());

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
