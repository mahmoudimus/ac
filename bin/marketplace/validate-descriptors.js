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


downloader.run({
    descriptorDownloadedCallback: function (result, body, opts) {
        if (result.type !== 'json') {
            console.log(result.addon.key, "XML descriptor not supported");
            return;
        }

        var descriptor = JSON.parse(body);

        validator.validateDescriptor(descriptor, jiraSchema, function (errors) {
            if (errors) {
                console.error(result.addon.key.red + " descriptor does not validate");
                if (!opts.quiet) {
                    console.log(util.inspect(errors, {colors: true, depth: 5}));
                }
            }
        });
    }
});

