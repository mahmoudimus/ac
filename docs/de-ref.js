#!/usr/bin/env node

var _ = require('lodash');
var jsonPath = require('JSONPath').eval;
var fs = require('fs-extra');
var program = require('commander');

program
    .option('-d, --debug', 'Output debug information')
    .parse(process.argv);

var debug = program.debug ? console.log : function () {/* no-op */
};

var sourceSchemaDirectory = "../plugin/target/classes/";
var targetSchemaDirectory = "schema/";

var files = {
    jiraSchema:       'schema/jira-schema.json',
    confluenceSchema: 'schema/confluence-schema.json',
    jiraScopes:       'com/atlassian/connect/scopes.jira.json',
    confluenceScopes: 'com/atlassian/connect/scopes.confluence.json',
    commonScopes:     'com/atlassian/connect/scopes.common.json'
};

/**
 * Schema generate puts the slugs under properties. That jacks things up, moving it up a notch.
 */
function moveSlugUpFromProperties(object, parent, parentName) {
    if (object == null) return;

    if (typeof object === 'object') {
        // if we're in a property collection &
        // we have a slug, move it to the parent
        if (parentName == "properties" && object.slug)
        {
            parent.slug = object.slug;
            delete object.slug;
        }

        for (var prop in object) {
            moveSlugUpFromProperties(object[prop], object, prop);
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

for (var file in files) {
    var source = sourceSchemaDirectory + files[file];
    var target = targetSchemaDirectory + files[file];

    debug("copying", source, "to", target);

    var sourceJson = fs.readJsonSync(source);

    // only need to dereference the schemas, save a few ms on the step
    if (file.match(/Schema$/)) {
        moveSlugUpFromProperties(sourceJson);
        dereference(sourceJson, sourceJson, "$");
    }

    fs.outputJsonSync(target, sourceJson);
}
