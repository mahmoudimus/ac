
var _ = require('lodash');
var jsonPath = require('JSONPath').eval;
var fs = require('fs-extra');

var sourceSchemaDirectory = "../plugin/target/classes/";
var targetSchemaDirectory = "schema/";

var files = {
    jiraSchema:       'schema/jira-schema.json',
    confluenceSchema: 'schema/confluence-schema.json',
    jiraScopes:       'com/atlassian/connect/scopes.jira.json',
    confluenceScopes: 'com/atlassian/connect/scopes.confluence.json',
    commonScopes:     'com/atlassian/connect/scopes.common.json'
};

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

exports.run = function() {
    for (var file in files) {
        var source = sourceSchemaDirectory + files[file];
        var target = targetSchemaDirectory + files[file];

        var sourceJson = fs.readJsonSync(source);

        // only need to dereference the schemas, save a few ms on the step
        if (file.match(/Schema$/)) {
            renamePropertyShortClassNameToId(sourceJson);
            dereference(sourceJson, sourceJson, "$");
            delete sourceJson.definitions;
        }

        fs.outputJsonSync(target, sourceJson);
    }
}
