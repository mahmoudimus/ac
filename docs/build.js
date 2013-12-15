#!/usr/bin/env node

var _ = require('lodash');
var fs = require('fs-extra');
var util = require('util');
var jiraSchema = require('../plugin/target/classes/schema/jira-schema.json');
var confluenceSchema = require('../plugin/target/classes/schema/confluence-schema.json');
var renderMarkdown = require("markdown-js").markdown;
var fork = require("child_process").fork;

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

function findNestedModules(ids) {
    return _.filter(entities.nested, function (val) {
        return ids.indexOf(val.id) > -1;
    });
}

entities.jiraModules = findNestedModules(jiraTypes);
entities.confluenceModules = findNestedModules(confluenceTypes);
entities.nested = _.difference(entities.nested, entities.jiraModules, entities.confluenceModules);

console.log(util.inspect(entities, {depth: 5}));

var buildDir = "./target";
var genSrcPrefix = buildDir + "/gensrc/";

fs.deleteSync(buildDir);

_.each(["public", "package.json", "node_modules"], function (requiredFile) {
    fs.copySync(requiredFile, genSrcPrefix + requiredFile);
});

// write out our file structure
_.each(entities, function(entitySet, parentKey) {
    _.each(entitySet, function(entity) {
        fs.outputFileSync(
            genSrcPrefix + 'public/modules2/' + parentKey + '/' + entity.id +'.md',
            "Placeholder - this file indicates static web directory structure and is not actually used in rendering.\n\n" +
            "Here's the model JSON for this file, y'know for debugging and stuff:\n\n" +
            JSON.stringify(entity, null, 2)
        );
    });
});

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

function sortAndWriteOutProperties(schema, product)
{
    schema.properties.modules.properties = sortModules(schema.properties.modules.properties);
    harpGlobals.globals.schemas[product] = schema;
    for (var k in schema.properties.modules.properties) {
        fs.outputFileSync(genSrcPrefix + "public/modules/" + product + '/' + k +'.md', String(schema.properties.modules.properties[k].items.description).replace(/\n /g,"\n"));
    }
}

sortAndWriteOutProperties(jiraSchema, 'jira');
sortAndWriteOutProperties(confluenceSchema, 'confluence');

// Store schema info into Harp globals
fs.outputFileSync(genSrcPrefix + 'harp.json', JSON.stringify(harpGlobals,null,2));

fork('./node_modules/harp/bin/harp', ["-o", "../www", "compile"], {'cwd': genSrcPrefix});