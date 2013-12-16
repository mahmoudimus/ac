#!/usr/bin/env node

var _ = require('lodash');
var fs = require('fs-extra');
var util = require('util');
var renderMarkdown = require("markdown-js").markdown;
var fork = require("child_process").fork;
var chokidar = require("chokidar");

var buildDir = "./target";
var genSrcPrefix = buildDir + "/gensrc/";

var srcFiles = ["public", "package.json"];

var jiraSchemaPath = '../plugin/target/classes/schema/jira-schema.json';
var confluenceSchemaPath = '../plugin/target/classes/schema/confluence-schema.json';

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

function uniqueArrayTypes(moduleList) {
    return _.unique(_.pluck(moduleList, "arrayType"));
}

function findModulesWithId(entities, ids) {
    return _.filter(entities, function (val) {
        return ids.indexOf(val.id) > -1;
    });
}

function copySrcFiles(filenames) {
    if (typeof filenames === "string") filenames = [filenames];
    _.each(filenames, function (filename) {
        fs.copySync(filename, genSrcPrefix + filename);
    });
}

function readJson(path) {
    return JSON.parse(fs.readFileSync(path, 'utf8'));
}

function rebuildHarpSite() {

    fs.deleteSync(buildDir);

    var jiraSchema = readJson(jiraSchemaPath);
    var confluenceSchema = readJson(confluenceSchemaPath);

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

    var jiraTypes = uniqueArrayTypes(entities.jiraModuleList);
    var confluenceTypes = uniqueArrayTypes(entities.confluenceModuleList);

    entities.jira = findModulesWithId(entities.fragment, jiraTypes);
    entities.confluence = findModulesWithId(entities.fragment, confluenceTypes);
    entities.fragment = _.difference(entities.fragment, entities.jira, entities.confluence);

    copySrcFiles(srcFiles);
    copySrcFiles("node_modules");

    // write out our file structure

    var keyToPath = {
        root: "modules",
        jira: "modules/jira",
        confluence: "modules/confluence",
        fragment: "modules/fragment"
    };

    var entityData = {};
    var entityLinks = {};

    _.each(entities, function(entitySet, parentKey) {
        var pathMapping = keyToPath[parentKey];
        if (pathMapping) {
            entityData[parentKey] = {};
            _.each(entitySet, function(entity) {
                entity = _.clone(entity);
                entityData[parentKey][entity.id] = entity;
                entity.selfLink = pathMapping + '/' + entity.id;

                entityLinks[entity.id] = entity.selfLink;

                var placeholder =
                        "Placeholder - this file indicates static web directory " +
                        "structure and is not actually used in rendering.\n\n" +
                        "Here's the model JSON for this file, y'know for debugging " +
                        "and stuff:\n\n" + JSON.stringify(entity, null, 2);

                var filePath = genSrcPrefix + 'public/' + entity.selfLink +'.md';

                fs.outputFileSync(filePath, placeholder);
            });
        }
    });

    var harpGlobals = require('./globals.json');

    harpGlobals.globals.entityLinks = entityLinks;
    harpGlobals.globals.entities = entityData;
    harpGlobals.globals.schemas = {};

    // Store schema info into Harp globals
    fs.outputFileSync(genSrcPrefix + 'harp.json', JSON.stringify(harpGlobals,null,2));
}

function startHarpServerAndWatchSrcFiles() {
    var harpServer;
    var restarting = false;

    function startHarpServer() {
        return fork('./node_modules/harp/bin/harp', ["server"], {'cwd': genSrcPrefix});
    }

    function restartHarpServer() {
        if (restarting) return;

        restarting = true;
        harpServer.on('exit', function() {
            rebuildHarpSite();
            harpServer = startHarpServer();
            restarting = false;
        });
        harpServer.kill();
    }

    harpServer = startHarpServer();

    var watchedFiles = srcFiles.concat(jiraSchemaPath, confluenceSchemaPath);

    var watcher = chokidar.watch(watchedFiles, {persistent:true});
    _.each(['add', 'addDir', 'change', 'unlink', 'unlinkDir'], function(event) {
        watcher.on(event, function(path) {
            console.log(event + " on " + path + "! Rebuilding..");
            restartHarpServer();
        });
    });
}

function compileHarpSources() {
    fork('./node_modules/harp/bin/harp', ["-o", "../www", "compile"], {'cwd': genSrcPrefix});
}

rebuildHarpSite();

if (process.argv.length > 2 && process.argv[2].toLowerCase().indexOf("serve") === 0) {
    startHarpServerAndWatchSrcFiles()
} else {
    compileHarpSources();
}

