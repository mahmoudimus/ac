#!/usr/bin/env node

var marketplace = require('./marketplace'),
    async = require('async'),
    request = require('request'),
    _ = require('lodash'),
    fs = require('fs'),
    path = require('path'),
    extend = require('node.extend'),
    colors = require('colors');

var marketplaceOpts = {
        cliOptsCallback: function (nomnom) {
            return nomnom.option('cache', {
                help: 'Use downloaded descriptor cache',
                abbr: 'c',
                flag: true
            });
        },
        downloadDirectory: path.resolve(__dirname, "./descriptors"),
        marketplaceAddonCallback: function (addon, opts) {
            var descriptorUrl = _.find(addon.version.links, {
                'rel': 'descriptor'
            }).href;

            marketplace.requestQueue().push({
                self: this,
                executor: downloadDescriptor,
                args: [opts, addon, descriptorUrl]
            }, opts.descriptorDownloadedCallback);
        }
    };


function downloadDescriptor(opts, addon, descriptorUrl, callback) {
    var addonKey = addon.pluginKey;
    if (opts.preDescriptorDownloadedCallback) {
        opts.preDescriptorDownloadedCallback({
            addon: {
                key: addonKey,
                listing: addon
            }
        }, opts);
    }

    var type = 'json';
    var filename = opts.downloadDirectory + '/' + addonKey + '-descriptor.' + type;
    if (opts.cache && fs.existsSync(filename)) {
        callback && callback({
            addon: {
                key: addonKey,
                listing: addon
            },
            descriptorFilename: filename,
            type: type,
        }, fs.readFileSync(filename), opts);
    } else {
        request({
            uri: descriptorUrl,
            method: "GET",
            auth: opts.auth
        }, function(error, response, body) {
            if (error || response.statusCode < 200 || response.statusCode >= 300) {
                console.log(("" + response.statusCode).red, "Unable to download descriptor for add-on", addonKey);
                callback(error);
            } else {
                type = 'xml';
                try {
                    JSON.parse(body);
                    type = 'json';
                } catch (e) {}

                if (!opts.type || opts.type === type) {
                    var filename = opts.downloadDirectory + '/' + addonKey + '-descriptor' + "." + type;
                    fs.writeFile(filename, body, function(err) {
                        if (err) {
                            console.log("Unable to write descriptor for add-on " + addonKey + " to disk", err);
                            callback(err);
                            return;
                        }

                        callback && callback({
                            addon: {
                                key: addonKey,
                                listing: addon
                            },
                            descriptorFilename: filename,
                            type: type,
                        }, body, opts);
                    });
                } else if (opts.debug) {
                    var t = "Ignored add-on " + addonKey + " (" + type + ")";
                    console.log(t.grey);
                }
            }
        });
    }
}

exports.run = function(runOpts) {
    var opts = extend({}, marketplaceOpts);
    opts = extend(opts, runOpts);

    if (!fs.existsSync(opts.downloadDirectory)) {
        fs.mkdirSync(opts.downloadDirectory);
    }

    marketplace.run(opts);
}

if (require.main === module) {
    exports.run();
}