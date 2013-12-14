#!/usr/bin/env node

var _ = require('lodash');
var fs = require('fs-extra');
var util = require('util');
var jiraSchema = require('../plugin/target/classes/schema/jira-schema.json');
var confluenceSchema = require('../plugin/target/classes/schema/confluence-schema.json');
var renderMarkdown = require("markdown-js").markdown;

function storeEntity(obj, key, bucket) {
    // store entities with ids if they have it, or key if they don't
    var id = obj.id || (obj.items && key);
    if (id && !bucket[id]) {
        bucket[id] = obj;
    }
    return bucket;
}

function findEntities(schemaProperties) {
    var bucket = {};
    _.forEach(schemaProperties, function(val, key) {
        if (typeof val === "object") {
            storeEntity(val, key, bucket);
        }
    });
    return bucket;
}

function findEntitiesRecursively(json, bucket) {
    bucket = bucket || {};
    _.forEach(json, function(val, key) {
        if (typeof val === "object") {
            storeEntity(val, key, bucket);
            findEntitiesRecursively(val, bucket);
        }
    });
    return bucket;
}

function collapseArrayAndObjectProperties(properties, required) {
    return _.map(properties, function(property, id) {
        switch (property.type) {
            case "array":
                property.id = id;
                property = _.pick(property, ["id", "type", "title"]);
                break;
            case "object":
                property = _.pick(property, ["id", "type", "title"]);
                break;
            default:
                // primitive
                break;
        }

        if (required && required.indexOf(id) > -1) {
            property.required = true;
        }
        property.key = id;

        return property;
    });
}

function schemaToModel(schemaEntity, id) {
    var name = schemaEntity.title || id;
    var description = renderMarkdown(schemaEntity.description || name);

    var model = {
        id: id,
        name: name,
        description: description,
        type: schemaEntity.type
    };

    if (model.type === 'array') {
        model.arrayType = schemaEntity.items.id;
    } else {
        model.properties = collapseArrayAndObjectProperties(schemaEntity.properties, schemaEntity.required);
    }

    return model;
}

var entities = {
    topLevel: findEntities(jiraSchema.properties),
    jiraModuleList: findEntities(jiraSchema.properties.modules.properties),
    confluenceModuleList: findEntities(confluenceSchema.properties.modules.properties)
};

var allEntities = _.extend(findEntitiesRecursively(jiraSchema), findEntitiesRecursively(confluenceSchema));
entities.nested = _.omit(allEntities, _.keys(entities.topLevel), _.keys(entities.jiraModuleList), _.keys(entities.confluenceModuleList));

entities = _.mapValues(entities, function(val) {
    return _.map(val, schemaToModel);
});

function uniqueArrayTypes(moduleList) {
    return _.unique(_.pluck(moduleList, "arrayType"));
}

var jiraTypes = uniqueArrayTypes(entities.jiraModuleList);
var confluenceTypes = uniqueArrayTypes(entities.confluenceModuleList);
var commonTypes = _.intersection(jiraTypes, confluenceTypes);
jiraTypes = _.difference(jiraTypes, commonTypes);
confluenceTypes = _.difference(confluenceTypes, commonTypes);

function extractNestedModules(ids) {
    var extracted = _.filter(entities.nested, function (val) {
        return ids.indexOf(val.id) > -1;
    });
    entities.nested = _.difference(entities.nested, extracted);
    return extracted;
}

entities.commonModules = extractNestedModules(commonTypes);
entities.jiraModules = extractNestedModules(jiraTypes);
entities.confluenceModules = extractNestedModules(confluenceTypes);

console.log(util.inspect(entities, {depth: 5}));

// OLD STUFF BELOW HERE

var harpGlobals = require('./globals.json');

harpGlobals.globals.entities = entities;
harpGlobals.globals.schemas = {};

// Sort capabilities so that they show up in alpha order
function sortModules(modules) {
    var keys = Object.keys(modules).sort();
    var obj = {};
    for(var i = 0; i < keys.length; i++) {
        obj[keys[i]] = modules[keys[i]];
    }
    return obj;
}

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
        console.log("Wrote ./harp.json");
    }
});
