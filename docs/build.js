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
    root: findEntities(jiraSchema.properties),
    jiraModuleList: findEntities(jiraSchema.properties.modules.properties),
    confluenceModuleList: findEntities(confluenceSchema.properties.modules.properties)
};

var allEntities = _.extend(findEntitiesRecursively(jiraSchema), findEntitiesRecursively(confluenceSchema));
entities.fragment = _.omit(allEntities, _.keys(entities.root), _.keys(entities.jiraModuleList), _.keys(entities.confluenceModuleList));

entities = _.mapValues(entities, function(val) {
    return _.map(val, schemaToModel);
});

function uniqueArrayTypes(moduleList) {
    return _.unique(_.pluck(moduleList, "arrayType"));
}

var jiraTypes = uniqueArrayTypes(entities.jiraModuleList);
var confluenceTypes = uniqueArrayTypes(entities.confluenceModuleList);

function findNestedModules(ids) {
    return _.filter(entities.fragment, function (val) {
        return ids.indexOf(val.id) > -1;
    });
}

entities.jira = findNestedModules(jiraTypes);
entities.confluence = findNestedModules(confluenceTypes);
entities.fragment = _.difference(entities.fragment, entities.jira, entities.confluence);

console.log(util.inspect(entities, {depth: 5}));

var buildDir = "./target";
var genSrcPrefix = buildDir + "/gensrc/";

fs.deleteSync(buildDir);

_.each(["public", "package.json", "node_modules"], function (requiredFile) {
    fs.copySync(requiredFile, genSrcPrefix + requiredFile);
});

// write out our file structure

var keyToPath = {
    root: "modules",
    jira: "modules/jira",
    confluence: "modules/confluence",
    fragment: "modules/fragment"
};

var entityData = {};

_.each(entities, function(entitySet, parentKey) {
    var pathMapping = keyToPath[parentKey];
    if (pathMapping) {
        entityData[parentKey] = {};
        _.each(entitySet, function(entity) {
            entity = _.clone(entity);
            entityData[parentKey][entity.id] = entity;
            entity.selfLink = pathMapping + '/' + entity.id;
            console.log(entity.id + " selfLink: " + entity.selfLink);

            var placeholder =
                    "Placeholder - this file indicates static web directory " +
                    "structure and is not actually used in rendering.\n\n" +
                    "Here's the model JSON for this file, y'know for debugging " +
                    "and stuff:\n\n" + JSON.stringify(entity, null, 2);

            var filePath = genSrcPrefix + 'public/' + entity.selfLink +'.md';

            console.log("Writing to " + filePath);

            fs.outputFileSync(filePath, placeholder);
        });
    }
});

var harpGlobals = require('./globals.json');

harpGlobals.globals.entities = entityData;
harpGlobals.globals.schemas = {};

// Sort capabilities so that they show up in alpha order
//function sortModules(modules) {
//    var keys = Object.keys(modules).sort();
//    var obj = {};
//    for(var i = 0; i < keys.length; i++) {
//        obj[keys[i]] = modules[keys[i]];
//    }
//    return obj;
//}
//
//function sortAndWriteOutProperties(schema, product)
//{
//    schema.properties.modules.properties = sortModules(schema.properties.modules.properties);
//    harpGlobals.globals.schemas[product] = schema;
//    for (var k in schema.properties.modules.properties) {
//        fs.outputFileSync(genSrcPrefix + "public/modules/" + product + '/' + k +'.md', String(schema.properties.modules.properties[k].items.description).replace(/\n /g,"\n"));
//    }
//}
//
//sortAndWriteOutProperties(jiraSchema, 'jira');
//sortAndWriteOutProperties(confluenceSchema, 'confluence');

// Store schema info into Harp globals
fs.outputFileSync(genSrcPrefix + 'harp.json', JSON.stringify(harpGlobals,null,2));

if (process.argv.length > 2 && process.argv[2].toLowerCase().indexOf("serve") === 0) {
    fork('./node_modules/harp/bin/harp', ["server"], {'cwd': genSrcPrefix});
} else {
    fork('./node_modules/harp/bin/harp', ["-o", "../www", "compile"], {'cwd': genSrcPrefix});
}

