#!/usr/bin/env node

var _ = require('lodash');
var fs = require('fs-extra');
var util = require('util');
var jiraSchema = require('../plugin/target/classes/schema/jira-schema.json');
var confluenceSchema = require('../plugin/target/classes/schema/confluence-schema.json');

function storeEntity(obj, bucket) {
    // store entities with ids
    var id = obj.id || (obj.items && obj.items.id);
    if (id && !bucket[id]) {
        bucket[id] = obj;
    }
    return bucket;
}

function findEntities(schemaProperties) {
    var bucket = {};
    _.forEach(schemaProperties, function(val) {
        if (typeof val === "object") {
            storeEntity(val, bucket);
        }
    });
    return bucket;
}

function findEntitiesRecursively(json, bucket) {
    bucket = bucket || {};
    _.forEach(json, function(val) {
        if (typeof val === "object") {
            storeEntity(val, bucket);
            findEntitiesRecursively(val, bucket);
        }
    });
    return bucket;
}

var entityRoots = {
    topLevel: findEntities(jiraSchema.properties),
    jira: findEntities(jiraSchema.properties.modules.properties),
    confluence: findEntities(confluenceSchema.properties.modules.properties)
};

var allRoots = _.extend(findEntitiesRecursively(jiraSchema), findEntitiesRecursively(confluenceSchema));

entityRoots.nested = _.omit(allRoots, _.keys(entityRoots.topLevel), _.keys(entityRoots.jira), _.keys(entityRoots.confluence));

console.log(util.inspect(entityRoots, {depth: 3}));


// OLD STUFF BELOW HERE

var harpGlobals = require('./harp.json');

// Sort capabilities so that they show up in alpha order
function sortModules(modules) {
    var keys = Object.keys(modules).sort();
    var obj = {};
    for(var i = 0; i < keys.length; i++) {
        obj[keys[i]] = modules[keys[i]];
    }
    return obj;
}

harpGlobals.globals.schemas = {};

jiraSchema.properties.modules.properties = sortModules(jiraSchema.properties.modules.properties);
harpGlobals.globals.schemas.jira = jiraSchema;
for (var k in jiraSchema.properties.modules.properties) {
    fs.outputFile('./public/modules/jira/'+k+'.md', String(jiraSchema.properties.modules.properties[k].items.description).replace(/\n /g,"\n"));
}

confluenceSchema.properties.modules.properties = sortModules(confluenceSchema.properties.modules.properties);
harpGlobals.globals.schemas.confluence = confluenceSchema;
for (var k in confluenceSchema.properties.modules.properties) {
    fs.outputFile('./public/modules/confluence/'+k+'.md', String(confluenceSchema.properties.modules.properties[k].items.description).replace(/\n /g,"\n"));
}




// Store schema info into Harp globals
fs.writeFile('./harp.json', JSON.stringify(harpGlobals,null,2), function(err) {
    if(err) {
        console.log(err);
    } else {
        console.log("Globals written");
    }
});
