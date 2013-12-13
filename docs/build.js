#!/usr/bin/env node

var fs = require('fs-extra');
var harpGlobals = require('./harp.json');
var jiraSchema = require('../plugin/target/classes/schema/jira-schema.json');
var confluenceSchema = require('../plugin/target/classes/schema/confluence-schema.json');

// Sort modules so that they show up in alpha order
function sortModules(schema, modules) {
    var keys = Object.keys(modules).sort();
    var obj = {};
    for(var i = 0; i < keys.length; i++) {
        obj[keys[i]] = schema.properties.modules.properties[keys[i]];
    }
    return obj;
}

harpGlobals.globals.schemas = {};

jiraSchema.properties.modules.properties = sortModules(jiraSchema, jiraSchema.properties.modules.properties);
harpGlobals.globals.schemas.jira = jiraSchema;
for (var k in jiraSchema.properties.modules.properties) {
    fs.outputFile('./public/modules/jira/'+k+'.md', String(jiraSchema.properties.modules.properties[k].items.description).replace(/\n /g,"\n"));
};

confluenceSchema.properties.modules.properties = sortModules(confluenceSchema, confluenceSchema.properties.modules.properties);
harpGlobals.globals.schemas.confluence = confluenceSchema;
for (var k in confluenceSchema.properties.modules.properties) {
    fs.outputFile('./public/modules/confluence/'+k+'.md', String(confluenceSchema.properties.modules.properties[k].items.description).replace(/\n /g,"\n"));
};


// Store schema info into Harp globals
fs.writeFile('./harp.json', JSON.stringify(harpGlobals,null,2), function(err) {
    if(err) {
        console.log(err);
    } else {
        console.log("Globals written");
    }
});
