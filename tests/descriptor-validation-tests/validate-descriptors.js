#!/usr/bin/env node

var marketplace = require('atlassian-connect-marketplace-scripts/marketplace.js'),
    downloader = require('atlassian-connect-marketplace-scripts/download-descriptors.js'),
    validator = require('atlassian-connect-validator'),
    _ = require('lodash'),
    fs = require('fs'),
    path = require('path'),
    colors = require('colors'),
    util = require('util');

var baseSchemaFile = 'target/schema/shallow-schema.json',
    commonModuleSchemaFiles = [
        'target/schema/common-schema.json'
    ],
    productConfigurations = {
        confluence: {
            moduleSchemaFiles: [
                'target/schema/confluence-schema.json'
            ],
            outputSchemaFile: 'target/schema/confluence-global-schema.json'
        },
        jira: {
            moduleSchemaFiles: [
                'target/schema/jira-schema.json'
            ],
            outputSchemaFile: 'target/schema/jira-global-schema.json'
        }
    };

var buildSchema = function(productConfiguration) {
    console.log("Building schema...");

    ensureSchemaFilesExist(productConfiguration.moduleSchemaFiles);

    var schema = loadJsonFile(baseSchemaFile);
    var moduleSchemaFiles = commonModuleSchemaFiles.concat(productConfiguration.moduleSchemaFiles);
    var moduleSchemas = moduleSchemaFiles.map(loadJsonFile);

    schema.properties.modules.additionalProperties = false;
    schema.properties.modules.properties = moduleSchemas.map(function(schema) {
        return schema.properties;
    }).reduce(_.merge);
    schema.definitions = moduleSchemas.concat(schema).map(function(schema) {
        return schema.definitions;
    }).reduce(_.merge);

    saveJsonFile(schema, productConfiguration.outputSchemaFile);

    return schema;
}

var ensureSchemaFilesExist = function(productModuleSchemaFiles) {
    var schemaFiles = commonModuleSchemaFiles.concat(baseSchemaFile).concat(productModuleSchemaFiles);
    for (var i = 0; i < schemaFiles.length; i++) {
        if (!fileExists(schemaFiles[i])) {
            console.error("Could not load schema file " + schemaFiles[i] + ". Please build the project.");
            process.exit();
        }
    }
}

var fileExists = function(filePath) {
    return fs.existsSync(path.resolve(__dirname, filePath));
};

var loadJsonFile = function(filePath) {
    return JSON.parse(fs.readFileSync(path.resolve(__dirname, filePath)));
}

var saveJsonFile = function(object, filePath) {
    fs.writeFileSync(path.resolve(__dirname, filePath), JSON.stringify(object, null, 2))
}

var validationResults = [],
    schemas = _.mapValues(productConfigurations, buildSchema);

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
