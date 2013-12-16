#!/usr/bin/env node

var _ = require('lodash');
var fs = require('fs-extra');
var util = require('util');
var renderMarkdown = require("markdown-js").markdown;
var fork = require("child_process").fork;
var chokidar = require("chokidar");
var jsonPath = require("JSONPath").eval;

var buildDir = "./target";
var genSrcPrefix = buildDir + "/gensrc/";

var srcFiles = ["public", "package.json"];

var jiraSchemaPath = '../plugin/target/classes/schema/jira-schema.json';
var confluenceSchemaPath = '../plugin/target/classes/schema/confluence-schema.json';

function collapseArrayAndObjectProperties(properties, required) {
    return _.map(properties, function(property, id) {
        if (property.type === "array") {
            property.id = id;
            property.arrayType = property.items.type;
            if (property.arrayType === 'object') {
                property.arrayTypeIds = [];
                if (property.items.anyOf) {
                    _.each(property.items.anyOf, function (anyOf) {
                        property.arrayTypeIds.push(anyOf.id);
                    });
                } else if (property.items.id) {
                    property.arrayTypeIds.push(property.items.id);
                }
            }
            property = _.pick(property, ["id", "type", "title", "description", "arrayType", "arrayTypeIds"]);
        } else if (property.type === "object" && property.id) {
            // if there's no id, it means that any object is allowed here
            property = _.pick(property, ["id", "type", "title", "description"]);
        }

        if (required && required.indexOf(id) > -1) {
            property.required = true;
        }
        property.key = id;

        if (property.description) {
            property.description = renderMarkdown(property.description);
        }

        return property;
    });
}

function schemaToModel(schemaEntity) {
    var name = schemaEntity.title || schemaEntity.id;
    var description = renderMarkdown(schemaEntity.description || name);

    var model = {
        id: schemaEntity.id,
        name: name,
        slug: slugify(name),
        description: description,
        type: schemaEntity.type
    };

    if (model.type === 'array') {
        model.arrayType = schemaEntity.items.type;
        if (model.arrayType === 'object') {
            model.arrayTypeIds = [];
            if (schemaEntity.items.id) {
                model.arrayTypeIds.push(schemaEntity.items.id);
            } else if (schemaEntity.items.anyOf) {
                _.each(schemaEntity.items.anyOf, function (anyOf) {
                    model.arrayTypeIds.push(anyOf.id);
                });
            }
        }
    } else if (model.type === 'object') {
        model.properties = collapseArrayAndObjectProperties(schemaEntity.properties, schemaEntity.required);
    }

    return model;
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

function slugify(string) {
    return string
            .toLowerCase()
            .replace(/[^\w ]+/g,'')
            .replace(/ +/g,'-');
}

function writeEntitiesToDisk(entities, pathMappings) {
    var entityLinks = {};

    _.each(entities, function(entitySet, parentKey) {
        var pathMapping = pathMappings[parentKey];
        if (pathMapping) {
            _.each(entitySet, function(entity) {
                entity.selfLink = pathMapping + '/' + entity.slug;
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

    return entityLinks;
}

function findRootEntities(schemas) {
    var entities = jsonPath(schemas, "$.*.*[?(@.id)]");
    entities = _.map(entities, schemaToModel);
    entities = _.zipObject(_.pluck(entities, "slug"), entities);
    return entities;
}

function findJiraModules(schemas) {
    var entities = jsonPath(schemas, "$.jira.properties.modules.properties.*[?(@.id)]");
    entities = _.map(entities, schemaToModel);
    entities = _.zipObject(_.pluck(entities, "slug"), entities);
    return entities;
}

function findConfluenceModules(schemas) {
    var entities = jsonPath(schemas, "$.confluence.properties.modules.properties.*[?(@.id)]");
    entities = _.map(entities, schemaToModel);
    entities = _.zipObject(_.pluck(entities, "slug"), entities);
    return entities;
}

function findFragmentEntities(schemas) {
    var entities = jsonPath(schemas, "$.*.properties.modules.properties.*.items.properties..*");
    entities = _.filter(entities, function(obj) {
        // object must have an id and not be a primitive array
        return obj.id && (obj.type !== "array");
    });
    entities = _.map(entities, schemaToModel);
    entities = _.zipObject(_.pluck(entities, "slug"), entities);
    return entities;
}

function rebuildHarpSite() {

    fs.deleteSync(buildDir);

    var schemas = {
        jira: readJson(jiraSchemaPath),
        confluence: readJson(confluenceSchemaPath)
    };

    var entities = {
        root: findRootEntities(schemas),
        jira: findJiraModules(schemas),
        confluence: findConfluenceModules(schemas),
        fragment: findFragmentEntities(schemas)
    };

    copySrcFiles(srcFiles);
    copySrcFiles("node_modules");

    var entityLinks = writeEntitiesToDisk(entities, {
        root: "modules",
        jira: "modules/jira",
        confluence: "modules/confluence",
        fragment: "modules/fragment"
    });

    var harpGlobals = require('./globals.json');

    harpGlobals.globals = _.extend({
        entityLinks: entityLinks,
        entities: entities,
        schemas: schemas
    }, harpGlobals.globals);

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

