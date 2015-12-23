
var _ = require('lodash'),
    jsonPath = require('JSONPath').eval,
    fs = require('fs-extra'),
    schemaMerger = require('atlassian-connect-json-schema-utils/merge-schemas.js');

var sourceDirectory = "../plugin/target/classes/";
var targetDirectory = "target/";

var sourceSchemaSubDirectory = "schema/";
var targetSchemaSubDirectory = sourceSchemaSubDirectory;

var sourceScopeSubDirectory = "scope/";
var targetScopeSubDirectory = "scope/";

var schemaFiles = [
    'shallow-schema.json',
    'common-schema.json',
    'jira-schema.json',
    'confluence-schema.json'
];

var scopeFiles = [
    'common-whitelist.json',
    'confluence-whitelist.json',
    'jira-whitelist.json',
    'jira-software-whitelist.json'
];

function renamePropertyShortClassNameToId(object) {
    if (object == null) return;

    if (typeof object === 'object') {
        object.id = object.shortClassName;
        delete object.shortClassName;

        for (var prop in object) {
            renamePropertyShortClassNameToId(object[prop]);
        }
    }
}

function dereference(object, objectRoot, path) {
    if (object == null) return;

    if (typeof object === 'object') {
        for (var prop in object) {
            dereference(object[prop], objectRoot, path + "." + prop)
        }

        if (object['$ref']) {
            var reference = object['$ref'];
            // HACK: this is needed for other stuff
            // keep the { $ref: "#" } around.
            if (reference != "#") {
                delete object['$ref'];

                var jPath = reference.replace("#", "").split('/').join('.');
                var foundObj = jsonPath(objectRoot, "$" + jPath);
                // jsonPath returns an array, we want the first item
                if (foundObj && foundObj[0])
                    foundObj = foundObj[0];

                // copy junk over
                for (var property in foundObj)
                    // only copy if it's not already there
                    if (object[property] === undefined)
                        object[property] = foundObj[property];
            }
        }
    }
    else if (typeof object === 'array') {
        for (var i = 0; i < object.length; i++)
            dereference(object[i], objectRoot, path);
    }
}

var importSchemas = function() {
    _.forEach(schemaFiles, function(file) {
        var sourceJson = fs.readJsonSync(sourceDirectory + sourceSchemaSubDirectory + file);
        fs.outputJsonSync(targetDirectory + targetSchemaSubDirectory + file, sourceJson);

        renamePropertyShortClassNameToId(sourceJson);
        dereference(sourceJson, sourceJson, "$");
        delete sourceJson.definitions;
        fs.outputJsonSync(targetDirectory + targetSchemaSubDirectory + "deref-" + file, sourceJson);
    });

    schemaMerger.mergeSchemas();
}

var importScopes = function() {
    _.forEach(scopeFiles, function(file) {
        var sourceJson = fs.readJsonSync(sourceDirectory + sourceScopeSubDirectory + file);
        fs.outputJsonSync(targetDirectory + targetScopeSubDirectory + file, sourceJson);
    });
}

exports.run = function() {
    importSchemas();
    importScopes();
}
