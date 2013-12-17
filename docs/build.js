#!/usr/bin/env node

// TODO this file should be refactored out into a couple of libraries to separate the concerns of schema munging, static directory construction and harp server lifecycle

var _ = require('lodash');
var fs = require('fs-extra');
var util = require('util');
var renderMarkdown = require("markdown-js").markdown;
var fork = require("child_process").fork;
var chokidar = require("chokidar");
var jsonPath = require("JSONPath").eval;
var program = require("commander");

var buildDir = "./target";
var genSrcPrefix = buildDir + "/gensrc";

var srcFiles = ["public", "package.json"];

var jiraSchemaPath = '../plugin/target/classes/schema/jira-schema.json';
var confluenceSchemaPath = '../plugin/target/classes/schema/confluence-schema.json';

program
  .option('-s, --serve', 'Serve and automatically watch for changes')
  .option('-b, --baseUrl [url]', 'Set the base url for rendered links')
  .parse(process.argv);

/**
 * Transform the schema properties entry for a schema entity into a shallow list of primitives and references.
 */
function collapseArrayAndObjectProperties(properties, required, parentId) {
    return _.map(properties, function(property, id) {
        if (property.type === "array") {
            property.id = id;
            if (property.items && property.items["$ref"] === "#") {
                // self reference
                property.arrayType = 'object';
                property.arrayTypeIds = [parentId];
            } else {
                property.arrayType = property.items.type;
                if (property.arrayType === 'object') {
                    property.arrayTypeIds = [];
                    if (property.items.anyOf) {
                        _.each(property.items.anyOf, function (child) {
                            property.arrayTypeIds.push(child.id);
                        });
                    } else if (property.items.id) {
                        property.arrayTypeIds.push(property.items.id);
                    }
                }
            }
            property = _.pick(property, ["id", "type", "title", "description", "arrayType", "arrayTypeIds"]);
        } else if (property.type === "object" && property.id) {
            // if there's no id, it means that any object is allowed here
            property = _.pick(property, ["id", "type", "title", "description"]);
            // if there's no id, it means that any object is allowed here
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

/**
 * Transform a schema entity root into a model object, suitable for rendering.
 */
function entityToModel(schemaEntity) {
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
            if (schemaEntity.items.anyOf) {
                _.each(schemaEntity.items.anyOf, function (child) {
                    model.arrayTypeIds.push(child.id);
                });
            } else if (schemaEntity.items.id) {
                model.arrayTypeIds.push(schemaEntity.items.id);
            }
        }
    } else if (model.type === 'object') {
        model.properties = collapseArrayAndObjectProperties(schemaEntity.properties, schemaEntity.required, schemaEntity.id);
    }

    return model;
}

/**
 * Transform a collection of schema entities into model objects, suitable for rendering. The returned
 * object will contain model objects, keyed by their slugified title or id.
 */
function entitiesToModel(entities) {
    entities = util.isArray(entities) ? entities : [entities];
    entities = _.map(entities, entityToModel);
    entities = _.zipObject(_.pluck(entities, "slug"), entities);
    return entities;
}

/**
 * Copy the supplied files to the gensrc directory.
 */
function copyToGenSrc(filenames) {
    if (typeof filenames === "string") filenames = [filenames];
    _.each(filenames, function (filename) {
        fs.copySync(filename, genSrcPrefix + '/' + filename);
    });
}

/**
 * Convert the supplied string into a string suitable for use in a filename or url.
 */
function slugify(string) {
    return string
            .toLowerCase()
            .replace(/[^\w ]+/g,'')
            .replace(/ +/g,'-');
}

/**
 * Write the supplied entities out to the gensrc directory, using the pathMappings supplied.
 * Each key from the entities object must match a key in pathMappings, or it will not be written out.
 *
 * e.g. writeEntitiesToDisk({some_key: {..}, some_other_key: {..}}, {some_key: 'some/path', some_other_key: 'some/other/path'})
 */
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

                var filePath = genSrcPrefix + '/public/' + entity.selfLink +'.md';

                fs.outputFileSync(filePath, placeholder);
            });
        }
    });

    return entityLinks;
}

/**
 * Find module types at the root of the descriptor (lifecycle, vendor, etc.) and the descriptor root itself.
 */
function findRootEntities(schemas) {
    // find top level modules
    var entities = jsonPath(schemas, "$.*.*[?(@.id)]");
    // add the descriptor root itself
    entities.unshift(schemas.jira);
    return entitiesToModel(entities);
}

/**
 * Find module types supported by JIRA (webItemModuleBean, searchRequestViewModuleBean, etc.)
 */
function findJiraModules(schemas) {
    return entitiesToModel(jsonPath(schemas, "$.jira.properties.modules.properties.*[?(@.id)]"));
}

/**
 * Find module types supported by Confluence (webItemModuleBean, staticContentMacroModuleBean, etc.)
 */
function findConfluenceModules(schemas) {
    return entitiesToModel(jsonPath(schemas, "$.confluence.properties.modules.properties.*[?(@.id)]"));
}

/**
 * Find JSON fragments that only exist nested inside other module types.
 */
function findFragmentEntities(schemas) {
    var entities = jsonPath(schemas, "$.*.properties.modules.properties.*.items.properties..*");
    entities = _.filter(entities, function(obj) {
        // object must have an id and not be a primitive array
        return obj.id && (obj.type !== "array");
    });
    return entitiesToModel(entities);
}

/**
 * Delete the build dir, regenerate the model from the schema and rebuild the documentation.
 */
function rebuildHarpSite() {

    fs.deleteSync(buildDir);

    var schemas = {
        jira: fs.readJsonSync(jiraSchemaPath),
        confluence: fs.readJsonSync(confluenceSchemaPath)
    };

    var entities = {
        root: findRootEntities(schemas),
        jira: findJiraModules(schemas),
        confluence: findConfluenceModules(schemas),
        fragment: findFragmentEntities(schemas)
    };

    copyToGenSrc(srcFiles);
    copyToGenSrc("node_modules");

    var entityLinks = writeEntitiesToDisk(entities, {
        root: "modules",
        jira: "modules/jira",
        confluence: "modules/confluence",
        fragment: "modules/fragment"
    });

    var harpGlobals = fs.readJsonSync('./globals.json');

    harpGlobals.globals = _.extend({
        entityLinks: entityLinks,
        entities: entities,
        baseUrl: program.baseUrl || ''
    }, harpGlobals.globals);

    console.log("Base url is: " + harpGlobals.globals.baseUrl);

    fs.outputFileSync(genSrcPrefix + '/harp.json', JSON.stringify(harpGlobals, null, 2));
}

/**
 * Start the Harp server. Also sets up watches for all files used as inputs to the documentation and
 * triggers a rebuild if they change.
 */
function startHarpServerAndWatchSrcFiles() {
    var harpServer;
    var restarting = false;

    function startHarpServer() {
        return fork('./node_modules/harp/bin/harp', ["server"], {'cwd': genSrcPrefix});
    }

    function restartHarpServer() {
        if (restarting) return;

        console.log("Rebuilding site and restarting harp server..");

        restarting = true;
        harpServer.on('exit', function() {
            rebuildHarpSite();
            harpServer = startHarpServer();
            restarting = false;
        });

        harpServer.kill();
    }

    // debounce to ensure multiple saves don't kick off multiple rebuilds
    restartHarpServer = _.debounce(restartHarpServer, 2000);

    harpServer = startHarpServer();

    var watchedFiles = srcFiles.concat(jiraSchemaPath, confluenceSchemaPath);

    var watcher = chokidar.watch(watchedFiles, {
        persistent:true,
        ignoreInitial:true
    });

    _.each(['add', 'addDir', 'change', 'unlink', 'unlinkDir'], function(event) {
        watcher.on(event, function(path) {
            console.log(event + " on " + path + "!");
            restartHarpServer();
        });
    });
}

/**
 * Statically compile the documentation into build /www directory.
 */
function compileHarpSources() {
    fork('./node_modules/harp/bin/harp', ["-o", "../www", "compile"], {'cwd': genSrcPrefix});
}

rebuildHarpSite();

if (program.serve) {
    startHarpServerAndWatchSrcFiles()
} else {
    compileHarpSources();
}
