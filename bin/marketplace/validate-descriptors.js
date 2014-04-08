#!/usr/bin/env node

var downloader = require('./download-descriptors'),
    validator = require('atlassian-connect-validator'),
    _ = require('lodash'),
    fs = require('fs'),
    colors = require('colors'),
    util = require('util');

var jiraSchemaPath = '../../plugin/target/classes/schema/jira-schema.json',
    confluenceSchemaPath = '../../plugin/target/classes/schema/confluence-schema.json',
    jiraSchema,
    confluenceSchema;

if (!fs.existsSync(jiraSchemaPath) || !fs.existsSync(confluenceSchemaPath)) {
    console.error("No schema found in path. Please build Atlassian Connect.");
    process.exit();
} else {
    jiraSchema = JSON.parse(fs.readFileSync(jiraSchemaPath, 'utf8'));
    confluenceSchema = JSON.parse(fs.readFileSync(confluenceSchemaPath, 'utf8'));
}

var warned = false;

downloader.run({
    descriptorDownloadedCallback: function (result, body, opts) {
        if (result.type !== 'json') {
            if (!warned) {
                console.log("WARN:".yellow, result.addon.key, "XML descriptors are not supported");
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

        validator.validateDescriptor(descriptor, schema, function (errors) {
            if (errors) {
                console.error(result.addon.key.red + " descriptor does not validate", app);
                if (!opts.quiet) {
                    console.log(util.inspect(errors, {colors: true, depth: 5}));
                }
            }
        });
    }
});

