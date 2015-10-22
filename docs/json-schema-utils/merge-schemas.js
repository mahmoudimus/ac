#!/usr/bin/env node

var _ = require('lodash'),
    fs = require('fs'),
    path = require('path');

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

var mergeSchemas = function() {
    return _.mapValues(productConfigurations, mergeSchema);
}

var mergeSchema = function(productConfiguration) {
    console.log("Building schema...");

    ensureSchemaFilesExist(productConfiguration.moduleSchemaFiles);

    var schema = loadJsonFile(baseSchemaFile);
    var moduleSchemaFiles = commonModuleSchemaFiles.concat(productConfiguration.moduleSchemaFiles);
    var moduleSchemas = moduleSchemaFiles.map(loadJsonFile);
    schema.properties.modules = {
        type: "object",
        additionalProperties: false,
        properties: moduleSchemas.map(function(schema) {
            return schema.properties;
        }).reduce(_.merge)
    };
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
    return fs.existsSync(path.resolve(filePath));
};

var loadJsonFile = function(filePath) {
    return JSON.parse(fs.readFileSync(path.resolve(filePath)));
}

var saveJsonFile = function(object, filePath) {
    fs.writeFileSync(path.resolve(filePath), JSON.stringify(object, null, 2))
}

exports.mergeSchemas = mergeSchemas;