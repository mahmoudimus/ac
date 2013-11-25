#!/usr/bin/env node

var fs = require('fs-extra');
var harpGlobals = require('./harp.json');
var jiraSchema = require('../plugin/target/classes/schema/jira-schema.json');
var confluenceSchema = require('../plugin/target/classes/schema/confluence-schema.json');

// Sort capabilities so that they show up in alpha order
function sortCapabilities(capabilities) {
    var keys = Object.keys(capabilities).sort();
    var obj = {};
    for(var i = 0; i < keys.length; i++) {
        obj[keys[i]] = jiraSchema.properties.capabilities.properties[keys[i]];
    }
    return obj;
}

harpGlobals.globals.schemas = {}

jiraSchema.properties.capabilities.properties = sortCapabilities(jiraSchema.properties.capabilities.properties);
harpGlobals.globals.schemas.jira = jiraSchema;
for (var k in jiraSchema.properties.capabilities.properties) {
    fs.outputFile('./public/capabilities/jira/'+k+'.md', String(jiraSchema.properties.capabilities.properties[k].items.description).replace(/\n /g,"\n"));
};

confluenceSchema.properties.capabilities.properties = sortCapabilities(confluenceSchema.properties.capabilities.properties);
harpGlobals.globals.schemas.confluence = confluenceSchema;
for (var k in confluenceSchema.properties.capabilities.properties) {
    fs.outputFile('./public/capabilities/confluence/'+k+'.md', String(confluenceSchema.properties.capabilities.properties[k].items.description).replace(/\n /g,"\n"));
};


// Store schema info into Harp globals
fs.writeFile('./harp.json', JSON.stringify(harpGlobals,null,2), function(err) {
    if(err) {
        console.log(err);
    } else {
        console.log("Globals written");
    }
});
