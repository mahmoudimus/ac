#!/usr/bin/env node

var marketplace = require('./marketplace'),
    downloader = require('./download-descriptors'),
    validator = require('atlassian-connect-validator'),
    _ = require('lodash'),
    fs = require('fs'),
    path = require('path'),
    colors = require('colors'),
    util = require('util');

var baseSchemaFile = '../../plugin/target/classes/schema/shallow-schema.json',
    moduleSchemaFiles = [
        '../../plugin/target/classes/schema/common-schema.json',
        '../../plugin/target/classes/schema/confluence-schema.json',
        '../../plugin/target/classes/schema/jira-schema.json'
    ],
    builtSchemaFile = '../../plugin/target/classes/schema/global-schema.json'
    ;

var buildSchema = function() {
    console.log("Building schema...");
    ensureSchemaFilesExist();
    var baseSchema = loadJsonFile(baseSchemaFile);
    var moduleSchemas = moduleSchemaFiles.map(loadJsonFile);
    baseSchema.properties.modules.additionalProperties = false;
    baseSchema.properties.modules.properties = moduleSchemas.map(function(schema) {
        return schema.properties;
    }).reduce(_.merge);
    baseSchema.definitions = moduleSchemas.concat(baseSchema).map(function(schema) {
        return schema.definitions;
    }).reduce(_.merge);
    return baseSchema;
}

var ensureSchemaFilesExist = function() {
    var schemaFiles = moduleSchemaFiles.concat(baseSchemaFile);
    for (var schemaName in schemaFiles) {
        if (!fileExists(schemaFiles[schemaName])) {
            console.error("Could not load schema file " + file + ". Please build the project.");
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
    fs.writeFileSync(filePath, JSON.stringify(object, null, 2))
}

var validationResults = [],
    schema = buildSchema();

saveJsonFile(schema, builtSchemaFile);

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

