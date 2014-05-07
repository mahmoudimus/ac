#!/usr/bin/env node

var marketplace = require('./marketplace'),
    downloader = require('./download-descriptors'),
    validator = require('atlassian-connect-validator'),
    _ = require('lodash'),
    fs = require('fs'),
    path = require('path'),
    colors = require('colors'),
    util = require('util');

var jiraSchemaPath = '../../plugin/target/classes/schema/jira-schema.json',
    confluenceSchemaPath = '../../plugin/target/classes/schema/confluence-schema.json',
    jiraSchema,
    confluenceSchema;

var schemaExists = function(schemaPath) {
    return fs.existsSync(path.resolve(__dirname, schemaPath));
};

var loadSchema = function(schemaPath) {
    return JSON.parse(fs.readFileSync(path.resolve(__dirname, schemaPath), 'utf8'));
}

if (!schemaExists(jiraSchemaPath) || !schemaExists(confluenceSchemaPath)) {
    console.error("No schema found in path. Please build Atlassian Connect.");
    process.exit();
} else {
    jiraSchema = loadSchema(jiraSchemaPath);
    confluenceSchema = loadSchema(confluenceSchemaPath);
}

var warned = false,
    validationResults = [],
    counter = 0;

function validate(opts, addonKey, descriptorFilename, descriptor, schema, callback) {
    validator.validateDescriptor(descriptor, schema, function (errors) {
        var r = {
            "addon": addonKey,
            "descriptorUrl": descriptorFilename,
            "errors": errors || []
        };
        if (errors) {
            if (!opts.quiet && !opts.testReport) {
                console.log(util.inspect(r, {colors: true, depth: 5}));
            }
        }
        if (opts.testReport) {
            validationResults.push(r);
        }
        callback();
    });
}

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
        if (result.type !== 'json') {
            if (!warned) {
                console.log("WARN:".yellow, "XML descriptors are not supported");
                warned = true;
            }
            return;
        }

        var descriptor = JSON.parse(body);

        var compatibleApps = result.addon.listing.compatibleApplications;

        if (!compatibleApps) {
            console.error(result.addon.key.red, "no compatible apps", JSON.stringify(result.listing));
            return;
        }

        var app = _.pluck(compatibleApps, "key")[0];

        var schema = (app === "confluence" ? confluenceSchema : jiraSchema);

        marketplace.requestQueue().push({
            self: this,
            executor: validate,
            args: [opts, result.addon.key, result.descriptorFilename, descriptor, schema]
        }, function () {});
    }
});

