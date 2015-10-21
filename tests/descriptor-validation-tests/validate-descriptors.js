#!/usr/bin/env node

var marketplace = require('atlassian-connect-marketplace-scripts/marketplace.js'),
    downloader = require('atlassian-connect-marketplace-scripts/download-descriptors.js'),
    validator = require('atlassian-connect-validator'),
    schemaMerger = require('atlassian-connect-json-schema-utils/merge-schemas.js'),
    _ = require('lodash'),
    fs = require('fs'),
    colors = require('colors'),
    util = require('util');

var validationResults = [],
    schemas = schemaMerger.mergeSchemas();

function validate(opts, addonKey, descriptorFilename, descriptor, schema, callback) {
    validator.validateDescriptor(descriptor, schema, function (errors) {
        var r = {
            "addon": addonKey,
            "descriptorUrl": descriptorFilename,
            "version": descriptor.version,
            "errors": errors || []
        };
        if (errors) {
            if (!opts.quiet && !opts.testReport) {
                console.log(util.inspect(r, {colors: true, depth: 5}));
            }
        } else if (opts.debug) {
            console.log("Validated add-on " + descriptor.key);
        }
        if (opts.testReport) {
            validationResults.push(r);
        }
        callback();
    });
}

var incompatibleApplications = {};

var addIncompatibleApps = function(compatibleAppNameList) {
    compatibleAppNameList.forEach(function(appName){
        if (incompatibleApplications[appName]) {
            incompatibleApplications[appName]++;
        } else {
            incompatibleApplications[appName] = 1;
        }
    });
};

downloader.run({
    before: function (opts) {
        marketplace.requestQueue().drain = function () {
            if (opts.testReport) {
                var r = {
                    "results": validationResults
                };
                fs.writeFileSync(opts.testReport, JSON.stringify(r));
            }
        }
    },
    cliOptsCallback: function (nomnom) {
        return nomnom.option('testReport', {
            help: 'Outputs results to given file as test output'
        });
    },
    descriptorDownloadedCallback: function (result, body, opts) {

        var descriptor = JSON.parse(body);

        var compatibleApps = result.addon.listing.compatibleApplications;

        if (!compatibleApps) {
            console.error(result.addon.key.red, "no compatible apps provided", JSON.stringify(result.addon.listing));
            return;
        }

        var registeredCompatibleApps = ["jira", "confluence"];
        var listCompatibleAppKeys = [];
        for (var app in compatibleApps) {
            listCompatibleAppKeys.push(compatibleApps[app].key);
        }

        var intersectionOfCompatibleApps = listCompatibleAppKeys.filter(
                function(appKey) {
                    return registeredCompatibleApps.indexOf(appKey) != -1;
                }
        );

        if (intersectionOfCompatibleApps.length == 0) {
            addIncompatibleApps(listCompatibleAppKeys);
            return;
        }

        var app = _.pluck(compatibleApps, "key")[0];

        var schema = (app === "confluence" ? schemas.confluence : schemas.jira);

        marketplace.requestQueue().push({
            self: this,
            executor: validate,
            args: [opts, result.addon.key, result.descriptorFilename, descriptor, schema]
        }, function () {});
    }
});

process.on('exit', function() {
  if (incompatibleApplications !== {}) {
      console.log("Incompatible Types Skipped:".red);
      for(var appName in incompatibleApplications) {
          console.log(appName.red, ": " + incompatibleApplications[appName]);
      }
  }
});
