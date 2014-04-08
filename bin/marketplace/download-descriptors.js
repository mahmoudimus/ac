#!/usr/bin/env node

var marketplace = require('./marketplace'),
    async = require('async'),
    request = require('request'),
    _ = require('lodash'),
    fs = require('fs'),
    extend = require('node.extend'),
    colors = require('colors');

var downloadDestination = "descriptors/",
    marketplaceOpts = {
        marketplaceAddonCallback: function (addon, opts) {
            var descriptorUrl = _.find(addon.version.links, {
                'rel': 'descriptor'
            }).href;

            downloadQueue.push({
                opts: opts,
                addon: addon,
                descriptorUrl: descriptorUrl
            });
        }
    };


var downloadQueue = async.queue(function (task, callback) {
    downloadDescriptor(task.opts, task.addon.pluginKey, task.addon, task.descriptorUrl, callback);
}, 3);


if (!fs.existsSync(downloadDestination)) {
    fs.mkdirSync(downloadDestination);
}

function downloadDescriptor(opts, addonKey, addon, descriptorUrl, callback) {
    if (opts.preDescriptorDownloadedCallback) {
        opts.preDescriptorDownloadedCallback(addonKey, addon, descriptorUrl, opts);
    }
    request({
        uri: descriptorUrl,
        method: "GET",
        auth: opts.auth
    }, function(error, response, body) {
        if (error) {
            console.log("Unable to download descriptor for add-on", addonKey);
            callback(error);
        } else {
            var type = 'xml';
            try {
                JSON.parse(body);
                type = 'json';
            } catch (e) {}

            if (!opts.type || opts.type === type) {
                var filename = downloadDestination + addonKey + '-descriptor' + "." + type;
                fs.writeFile(filename, body, function(err) {
                    if (err) {
                        console.log("Unable to write descriptor for add-on " + addonKey + " to disk", err);
                        callback(err);
                        return;
                    }

                    if (opts.descriptorDownloadedCallback) {
                        opts.descriptorDownloadedCallback({
                            addon: {
                                key: addonKey,
                                listing: addon
                            },
                            descriptorFilename: filename,
                            type: type,
                        }, body, opts);
                    }

                    callback();
                });
            } else if (opts.debug) {
                var t = "Ignored add-on " + addonKey + " (" + type + ")";
                console.log(t.grey);
            }
        }
    });
}

exports.run = function(runOpts) {
    var opts = extend({}, marketplaceOpts);
    opts = extend(opts, runOpts);
    marketplace.run(opts);
}

if (require.main === module) {
    exports.run();
}