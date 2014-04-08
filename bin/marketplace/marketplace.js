#!/usr/bin/env node

var request = require('request'),
    _ = require('lodash'),
    async = require('async'),
    extend = require('node.extend'),
    nomnom = require('nomnom'),
    colors = require('colors'),
    util = require('util');

var selfExecute = require.main === module;

var downloadQueue = async.queue(function (task, callback) {
    var args = task.args;
    args.push(callback);
    task.executor.apply(task.self || this, args);
}, 3);

downloadQueue.drain = function () { console.log("all done"); }

var getAddonPage = function(opts, uri, callback) {
    if (opts.debug) {
        console.log("downloading: ".grey + opts.baseUrl + uri);
    }
    
    return request({
        uri: opts.baseUrl + uri,
        method: "GET",
        auth: opts.auth,
        json: true
    }, function(error, response, body) {
        if (error) {
            console.log("ERROR".red + ": Unable to retrieve marketplace add-ons", response, error);
            callback(error);
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
                        if (opts.debug || selfExecute) {
                            console.log(addon.pluginKey, "(" + type[typeColor] + ", " + status[statusColor] + ")", opts.debug ? (releaseDate + " " + url.grey) : "");
                        }
                    } catch (e) {
                        if (opts.debug) {
                            console.log(("" + e).red);
                            console.log(JSON.stringify(addon));
                        }
                        callback(e);
                    }
                }

                if (opts.marketplaceAddonCallback) {
                    opts.marketplaceAddonCallback(addon, opts);
                }
            });

            if (nextRequestUri) {
                var nextOpts = extend({}, opts);
                downloadQueue.push({
                    self: this,
                    executor: getAddonPage,
                    args: [nextOpts, nextRequestUri]
                }, function () { });
            }

            callback();
        }
    });
};

var getCliOpts = function(callback) {
    var nn = nomnom
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

    if (callback) {
        nn = callback(nn);
    }
    return nn;
}

var defaultOpts = {
    baseUrl: "https://marketplace.atlassian.com",
    uri: "/rest/1.0/plugins?hosting=ondemand&addOnType=three&limit=50"
}

exports.run = function(runOpts) {
    var nomnom = getCliOpts(runOpts ? runOpts.cliOptsCallback : false);

    var opts = extend({}, defaultOpts);
    opts = extend(opts, runOpts);
    opts = extend(opts, nomnom.parse());

    var uri = opts.uri;
    delete opts["uri"];

    if (opts.includePrivate) {
        uri = uri + "&includePrivate=true";
    }

    if (opts.user && opts.pass) {
        opts.auth = {
            username: opts.user,
            password: opts.pass
        }
    }

    if (opts.includePrivate && !opts.auth) {
        nomnom.help();
        console.log("ERROR: To retrieve private add-ons, credentials are required.");
        return;
    }

    if (opts.before) {
        opts.before(opts);
    }

    downloadQueue.push({
        self: this,
        executor: getAddonPage,
        args: [opts, uri]
    }, function () { });
};

exports.requestQueue = function () {
    return downloadQueue;
}

if (selfExecute) {
    exports.run();
}
