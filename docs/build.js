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
function collapseArrayAndObjectProperties(properties, required, parent) {
    return _.map(properties, function(property, id) {
        if (property.type === "array") {
            property.id = id;
            
            property.arrayType = property.items.type;
            if (property.arrayType === 'object') {
                property.arrayTypes = [];
                if (property.items.anyOf) {
                    _.each(property.items.anyOf, function (child) {
                        if(child["$ref"] === "#")
                        {
                            // self reference 
                            property.arrayTypes.push({id: parent.id, title: parent.name});
                        }
                        else
                        {
                            property.arrayTypes.push({
                                id: child.id,
                                title: child.title || child.id
                            });
                        }
                        
                    });
                } else if (property.items.id) {
                    property.arrayTypes.push({
                        id: property.items.id,
                        title: property.items.title || property.items.id
                    });
                }
            }

            property = _.pick(property, ["id", "type", "title", "description", "fieldDescription", "arrayType", "arrayTypes"]);
        } else if (property.type === "object" && property.id) {
            // if there's no id, it means that any object is allowed here
            property = _.pick(property, ["id", "type", "title", "description", "fieldDescription"]);
        }

        if (required && required.indexOf(id) > -1) {
            property.required = true;
        }
        property.key = id;

        // render description fields, if present
        if (property.description) {
            property.description = renderMarkdown(property.description);
        }
        if (property.fieldDescription) {
            property.fieldDescription = renderMarkdown(property.fieldDescription);
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
        slug: slugify(schemaEntity.pageName || name),
        description: description,
        type: schemaEntity.type
    };

    if (model.type === 'object') {
        model.properties = collapseArrayAndObjectProperties(schemaEntity.properties, schemaEntity.required, model);
        model.properties.sort(function(a, b) {
            // required then alpha
            if (a.required && !b.required) {
                return -1;
            }
            if (b.required && !a.required) {
                return 1;
            }
            a = (a.title || a.key).toLowerCase();
            b = (a.title || b.key).toLowerCase();
            if (a < b) return -1;
            if (a > b) return 1;
            return 0;
        });
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
    entities.sort(function(a, b) {
        if (a.name < b.name) return -1;
        if (a.name > b.name) return 1;
        return 0;
    });
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
    // exclude the module lists, they're rendered separately in findJiraModules etc.
    entities = _.filter(entities, function(entity) {return entity.id !== "moduleList";});
    // add the descriptor root itself (and make it the index.html for modules)
    schemas.jira.pageName = "index";
    entities.unshift(schemas.jira);
    return entitiesToModel(entities);
}

/**
 * Find module types supported by a particular product (webItemModuleBean, staticContentMacroModuleBean, etc.)
 */
function findProductModules(schemas, productId, productDisplayName) {
    // find product modules
    var productModules = jsonPath(schemas, "$." + productId + ".properties.modules.properties.*[?(@.id)]");

    var moduleList = schemas[productId].properties.modules;
    // the module list serves as our landing page for each product's modules
    moduleList.pageName = "index";
    moduleList.title = productDisplayName + " Module List";
    // make the module list the first entry
    productModules.unshift(moduleList);
    return entitiesToModel(productModules);
}

/**
 * Find JSON fragments that only exist nested inside other module types.
 */
function findFragmentEntities(schemas) {
    var entities = jsonPath(schemas, "$.*.properties.modules.properties.*.items.properties..*");
    entities = _.filter(entities, function(obj) {
        // object must have an id and not be a primitive array
        return obj && typeof obj === "object" && obj.id && obj.type === "object";
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
        jira: findProductModules(schemas, "jira", "JIRA"),
        confluence: findProductModules(schemas, "confluence", "Confluence"),
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
