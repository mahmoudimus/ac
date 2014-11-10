#!/usr/bin/env node

var request = require('request'),
    _ = require('lodash'),
    async = require('async'),
    extend = require('node.extend'),
    nomnom = require('nomnom'),
    moment = require('moment'),
    colors = require('colors'),
    util = require('util');

var selfExecute = require.main === module;

var downloadQueue = async.queue(function (task, callback) {
    var args = task.args;
    args.push(callback);
    task.executor.apply(task.self || this, args);
}, 3);

downloadQueue.drain = function () {
}

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
            console.log("ERROR".red + ": Unable to retrieve marketplace data", response, error);
            callback(error);
        } else {
            var series = body.series;

            var apps = {};

            var getApp = function (a) {
                var app = apps[a];
                if (!app) {
                    app = apps[a] = [];
                }
                return app;
            }

            var reportApp = function (product) {
                var p = apps[product];
                if (!p) {
                    console.log("fatal".red + ": No data for product " + product.yellow);
                    return;
                }
                console.log("Report: " + product);
                var grouped = _.groupBy(p, opts.groupBy || 'patch');

                var r = _.map(grouped, function (group, key) {
                    var count = _.reduce(group, function (memo, version) {
                        return memo + version.elements[0].count;
                    }, 0);
                    return { version: key, count: count };
                });

                var total = _.reduce(r, function (m, n) { return m + n.count; }, 0);

                _.each(r, function (v) {
                    var pct = (v.count / total * 100);
                    pct = Math.round(pct * 100) / 100
                    v.percentage = (pct) + "%";
                });

                // _.forEach(_.sortBy(r, 'version'), function (v) {
                //     console.log(v.version, v.count);
                // });
                console.log(_.sortBy(r, 'version'));
                console.log("Total: " + total);
            }

            _.forEach(series, function(s) {
                var name = s.name;
                var bits = name.split(' ');
                var product = bits[0].toLowerCase();
                var version = bits[1];
                var versionBits = version.split(/[\.-]/);

                var app = getApp(product);
                app.push({
                    version: version,
                    major: versionBits[0],
                    minor: versionBits[0] + "." + versionBits[1],
                    patch: version,
                    elements: s.elements
                });
            });

            _.forEach(opts.product, function (p) { reportApp(p) });
                
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
        .option('groupBy', {
            abbr: 'g',
            help: 'Group by major/minor/patch',
            choices: ['major', 'minor', 'patch'],
            default: 'patch'
        })
        .option('product', {
            abbr: 'P',
            help: 'Product',
            list: true,
            required: true
        })
        .option('quiet', {
            abbr: 'q',
            flag: true,
            help: 'Don\'t debug spam'
        });

    if (callback) {
        nn = callback(nn);
    }
    return nn;
}

var defaultOpts = {
    baseUrl: "https://marketplace.atlassian.com",
    uri: "/rest/2.0-beta/addons/com.atlassian.upm.atlassian-universal-plugin-manager-plugin/distribution/application/version?"
}

exports.run = function(runOpts) {
    var nomnom = getCliOpts(runOpts ? runOpts.cliOptsCallback : false);

    // last complete data is the previous monday.
    var today = moment(),
        day = +(today.format('d'));
    if (day == 0) {
        dayDifference = 13; // on a sunday, the monday previous to this one
    } else {
        dayDifference = day + 6;
    }
    var dataDate = today.subtract(dayDifference, 'days').format("YYYY-MM-DD");

    defaultOpts.uri = defaultOpts.uri + "start-date=" + dataDate + "&end-date=" + dataDate;

    var opts = extend({}, defaultOpts);
    opts = extend(opts, runOpts);
    opts = extend(opts, nomnom.parse());

    var uri = opts.uri;
    delete opts["uri"];

    if (opts.user && opts.pass) {
        opts.auth = {
            username: opts.user,
            password: opts.pass
        }
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
