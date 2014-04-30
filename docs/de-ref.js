#!/usr/bin/env node

var _ = require('lodash');
var jsonPath = require('jsonPath').eval;
var fs = require('fs-extra');
var program = require('commander');

program
  .option('-d, --debug', 'Output debug information')
  .parse(process.argv);

var debug = program.debug ? console.log : function() {/* no-op */};

var sourceSchemaDirectory = "../plugin/target/classes/";
var targetSchemaDirectory = "schema/";

var files = {
  jiraSchema:       'schema/jira-schema.json',
  confluenceSchema: 'schema/confluence-schema.json',
  jiraScopes:       'com/atlassian/connect/scopes.jira.json',
  confluenceScopes: 'com/atlassian/connect/scopes.confluence.json',
  commonScopes:     'com/atlassian/connect/scopes.common.json'
};

function dereference(object, objectRoot, path) {
  if (object == null) return;

  if (typeof object === 'object')
  {
    for(var prop in object) {
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
        for(var property in foundObj)
          object[property] = foundObj[property];
      }
    }
  } 
  else if (typeof object === 'array')
  {
    for(var i = 0; i < object.length; i++)
      dereference(object[i], objectRoot, path);
  }
}

for(var file in files) {
  var source = sourceSchemaDirectory + files[file];
  var target = targetSchemaDirectory + files[file];

  debug("copying", source, "to", target);

  var sourceJson = fs.readJsonSync(source);

  dereference(sourceJson, sourceJson, "$");

  fs.outputJsonSync(target, sourceJson);
}
