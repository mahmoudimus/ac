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
    return nomnom
            .option('cache', {
              help: 'Use downloaded descriptor cache',
              abbr: 'c',
              flag: true
            })
            .option('save', {
              help: 'Save descriptors to file',
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
  },
  before: function (opts) {
    if (opts.save && !fs.existsSync(opts.downloadDirectory)) {
      fs.mkdirSync(opts.downloadDirectory);
    }
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

  var filename = opts.downloadDirectory + '/' + addonKey + '-descriptor.json';
  if (opts.cache && fs.existsSync(filename)) {
    callback && callback({
      addon: {
        key: addonKey,
        listing: addon
      },
      descriptorFilename: filename
    }, fs.readFileSync(filename), opts);
  } else {
    request({
      uri: descriptorUrl,
      method: "GET",
      auth: opts.auth
    }, function(error, response, body) {
      if (error || response.statusCode < 200 || response.statusCode >= 300) {
        var message = response ? "" + response.statusCode : error.toString();
        console.log(message.red, "Unable to download descriptor for add-on", addonKey);
        callback(error);
      } else {
        var doCallback = function (body, opts, filename) {
          var _result = {
            addon: {
              key: addonKey,
              listing: addon
            }
          };
          if (filename) {
            _result.descriptorFilename = filename;
          }
          callback && callback(_result, body, opts);
        }

        if (opts.save) {
          var filename = opts.downloadDirectory + '/' + addonKey + '-descriptor.json';

          fs.writeFile(filename, body, function(err) {
            if (err) {
              console.log("Unable to write descriptor for add-on " + addonKey + " to disk", err);
              callback(err);
              return;
            }

            doCallback(body, opts, filename);
          });
        } else {
          doCallback(body, opts);
        }
      }
    });
  }
}

exports.run = function(runOpts) {
  var opts = extend({}, marketplaceOpts);
  opts = extend(opts, runOpts);

  marketplace.run(opts);
}

if (require.main === module) {
  exports.run();
}