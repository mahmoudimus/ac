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

function validate(opts, addonKey, descriptor, schema, callback) {
    validator.validateDescriptor(descriptor, schema, function (errors) {
        if (errors) {
            if (!opts.quiet && !opts.test) {
                console.log(util.inspect(errors, {colors: true, depth: 5}));
            }
        }
        if (opts.test) {
            validationResults.push({
                "addon": addonKey,
                "errors": errors || []
            });
        }
        callback();
    });
}

downloader.run({
    before: function (opts) {
        marketplace.requestQueue().drain = function () {
            var r = {
                "results": validationResults
            };
            console.log(JSON.stringify(r));
        }
    },
    cliOptsCallback: function (nomnom) {
        return nomnom.option('test', {
            flag: true,
            help: 'Outputs results as test output'
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
            args: [opts, result.addon.key, descriptor, schema]
        }, function () {});
    }
});

