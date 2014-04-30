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

var jiraSchemaSourcePath =       '../plugin/target/classes/schema/jira-schema.json';
var confluenceSchemaSourcePath = '../plugin/target/classes/schema/confluence-schema.json';

var jiraSchemaPath =       'schema/schema/jira-schema.json';
var confluenceSchemaPath = 'schema/schema/confluence-schema.json';
var jiraScopesPath =       'schema/com/atlassian/connect/scopes.jira.json';
var confluenceScopesPath = 'schema/com/atlassian/connect/scopes.confluence.json';
var commonScopesPath =     'schema/com/atlassian/connect/scopes.common.json';

program
  .option('-s, --serve', 'Serve and automatically watch for changes')
  .option('-d, --debug', 'Output debug information')
  .parse(process.argv);

var debug = program.debug ? console.log : function() {/* no-op */};

/**
 * Transform the schema properties entry for a schema entity into a shallow list of primitives and references.
 */
function collapseArrayAndObjectProperties(properties, required, parent) {
    return _.map(properties, function(property, id) {
        debug("\nCollapsed:\n", util.inspect(property, {depth: 2}));

        // prefer fieldTitle if present (provides better context)
        if (property.fieldTitle) {
            property.title = property.fieldTitle;
        }

        property.slug = slugify(property.title || property.slug);

        if (property.type === "array") {
            property.id = id;
            property.arrayType = property.items.type;

            if (property.arrayType === 'object') {
                property.arrayTypes = [];
                if (property.items.anyOf) {
                    _.each(property.items.anyOf, function (child) {
                        if (child["$ref"] === "#") {
                            // self reference
                            property.arrayTypes.push({
                                id: parent.id,
                                title: parent.name,
                                slug: parent.slug
                            });
                        } else {
                            var title = child.title || child.id;
                            property.arrayTypes.push({
                                id: child.id,
                                title: title,
                                slug: slugify(title)
                            });
                        }
                    });
                } else if (property.items.id) {
                    var title = property.items.title || property.items.id;
                    property.arrayTypes.push({
                        id: property.items.id,
                        title: title,
                        slug: slugify(title)
                    });
                }
            }

            property = _.pick(property, ["id", "type", "title", "slug", "description", "fieldDescription", "arrayType", "arrayTypes"]);
        } else if (property.type === "object" && property.id) {
            // if there's no id, it means that any object is allowed here
            property = _.pick(property, ["id", "type", "title", "slug", "description", "fieldDescription"]);
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

        debug("into:\n", util.inspect(property, {depth: 2}), "\n");

        return property;
    });
}

/**
 * Transform a schema entity root into a model object, suitable for rendering.
 */
function entityToModel(schemaEntity) {
    var name = schemaEntity.title || schemaEntity.slug;
    var description = renderMarkdown(schemaEntity.description || name);

    var model = {
        id: schemaEntity.slug,
        name: name,
        slug: slugify(schemaEntity.slug || schemaEntity.pageName || name),
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
            a = (a.title || a.slug || "").toLowerCase();
            b = (a.title || b.slug || "").toLowerCase();
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
    return string && string
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
                entityLinks[entity.slug] = entity.selfLink;

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
    var productModules = jsonPath(schemas, "$." + productId + ".properties.modules.properties.*");
    // unwrap array types
    productModules = _.map(productModules, function (moduleOrArray) {
        return moduleOrArray.type === "array" ? moduleOrArray.items : moduleOrArray;
    });
    // remove slugs or other junk
    productModules = _.filter(productModules, function(maybeAnObject) {
      return maybeAnObject.type === "object";
    });
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
 * Transform the restPaths entries from the raw scopes file into a lookup structure by path:
 * {
 *     "/project": {
 *         "GET": "READ",
 *         "POST": "WRITE"
 *     }
 * }
 */
function transformRestScopes(rawScopes, pathKeysProperty) {
    var scopes = {};
    _.each(rawScopes.scopes, function (scope) {
        _.each(scope[pathKeysProperty], function (restPathKey) {
            scopes[restPathKey] = scopes[restPathKey] || {};
            _.each(scope.methods, function (method) {
                scopes[restPathKey][method] = scope.key;
            });
        });
    });
    return scopes;
}

/**
 * All the REST paths end in "($|/.*)", which is distracting
 */
function removeTrailingPattern(path) {
    var patternPos = path.lastIndexOf('(');
    return patternPos > -1 ? path.substring(0, patternPos) : path;
}

/**
 * Generic transformer for RPC style APIs that iterates over one or more scope files
 * and invokes a callback for every entry in an array specified by 'keyProperty'. It looks
 * up the matching scope first and passes that into the callback as well.
 */
function convertScopesToViewModel(scopeDefinitions, pathsProperty, keyProperty, methodsProperty, sortKey, converter) {
    var apis = _.map(scopeDefinitions, function(scopeDefinition) {
        return _.map(scopeDefinition[pathsProperty], function (path) {
            var matchingScope = _.find(scopeDefinition.scopes, function (scope) {
                return (scope[keyProperty] && _.contains(scope[keyProperty], path.key))
            });
            return _.map(path[methodsProperty], function (method) {
                return converter(path, method, matchingScope)
            });
        });
    });
    return { apis: _.sortBy(_.flatten(apis), sortKey)};
}

/**
 * Transform the restPaths entries from the raw scopes files into
 * a model that can be easily rendered.
 *
 * It also includes the download scopes, as these are conceptually
 * equivalent to the rest scopes.
 *
 * The result is a sorted array of entries like this:
 * {
 *     "path" : "/api/{version}/resource",
 *     "id": "apiversionresource",
 *     "versions": ["1.0", "2.0"],
 *     "scopes": {
 *         "GET": "READ",
 *         "POST": "WRITE"
 *     }
 * }
 */
function convertRestScopesToViewModel(scopeDefinitions) {
    var restApis = _.map(scopeDefinitions, function(scopeDefinition) {
        var scopesByKey = transformRestScopes(scopeDefinition, "restPathKeys");
        return _.map(scopeDefinition.restPaths, function (restPath) {
            return _.map(restPath.basePaths, function (basePath) {
                var version = (restPath.versions.length > 0) ? "/{version}" : "";
                var path = "/rest/" + restPath.name + version + removeTrailingPattern(basePath);
                return {
                    path: path,
                    id: slugify(path),
                    versions: restPath.versions.sort(),
                    scopes: scopesByKey[restPath.key],
                    public: restPath.public
                }
            });
        });
    });
    // The below code can be removed once download scopes and rest scopes
    // share the same structure
    var downloadApis = _.map(scopeDefinitions, function(scopeDefinition) {
        var scopesByKey = transformRestScopes(scopeDefinition, "pathKeys");
        return _.map(scopeDefinition.paths, function (downloadPath) {
            return _.map(downloadPath.paths, function (basePath) {
                var path = removeTrailingPattern(basePath);
                return {
                    path: path,
                    id: slugify(path),
                    versions: [],
                    scopes: scopesByKey[downloadPath.key],
                    public: downloadPath.public
                }
            });
        });
    });
    return { apis: _.sortBy(_.flatten([restApis, downloadApis]), "path")};
}

/**
 * Transform the *rpcPaths entries from the raw scopes files into
 * a model that can be easily rendered.
 *
 * The result is a sorted array of entries like this:
 * {
 *     "method" : "getSomething",
 *     "id": "getsomething",
 *     "paths": ["/v1", "/v2"],
 *     "scope": "READ"
 * }
 */
function convertRpcScopesToViewModel(scopeDefinitions, pathsProperty, keyProperty) {
    return convertScopesToViewModel(scopeDefinitions, pathsProperty, keyProperty, "rpcMethods", "method",
        function (rpcPath, rpcMethod, scope) {
            return {
                method: rpcMethod,
                paths: rpcPath.paths,
                id: slugify(rpcMethod),
                scope: scope.key
            }
        });
}

function convertJsonRpcScopesToViewModel(scopeDefinitions) {
    return convertRpcScopesToViewModel(scopeDefinitions, "jsonRpcPaths", "jsonRpcPathKeys");
}

function convertSoapRpcScopesToViewModel(scopeDefinitions) {
    return convertRpcScopesToViewModel(scopeDefinitions, "soapRpcPaths", "soapRpcPathKeys");
}

function convertXmlRpcScopesToViewModel(scopeDefinitions) {
    return convertScopesToViewModel(scopeDefinitions, "xmlRpcPaths", "xmlRpcPathKeys", "rpcMethods", "method",
        function (rpcPath, rpcMethod, scope) {
            return _.map(rpcPath.prefixes, function (prefix) {
                var method = prefix + "." + rpcMethod;
                return {
                    method: method,
                    paths: ["/rpc/xmlrpc"],
                    id: slugify(method),
                    scope: scope.key
                }
            });
        })
}

/**
 * Delete the build dir, regenerate the model from the schema and rebuild the documentation.
 */
function rebuildHarpSite() {

    fs.deleteSync(buildDir);

    dereferenceSchema();
    compileJsDocs();

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

    var scopes = {
        jira: fs.readJsonSync(jiraScopesPath),
        confluence: fs.readJsonSync(confluenceScopesPath),
        common: fs.readJsonSync(commonScopesPath)
    };

    var scopesView = {
        confluence: {
            rest: convertRestScopesToViewModel([scopes.confluence, scopes.common]),
            jsonrpc: convertJsonRpcScopesToViewModel([scopes.confluence, scopes.common]),
            xmlrpc: convertXmlRpcScopesToViewModel([scopes.confluence, scopes.common])
        },
        jira: {
            rest: convertRestScopesToViewModel([scopes.jira, scopes.common]),
            jsonrpc: convertJsonRpcScopesToViewModel([scopes.jira, scopes.common]),
            soaprpc: convertSoapRpcScopesToViewModel([scopes.jira, scopes.common])
        }
    };

    debug("\nEntities:\n", util.inspect(entities, {depth: 3}), "\n");

    copyToGenSrc(srcFiles);
    copyToGenSrc("node_modules");

    var entityLinks = writeEntitiesToDisk(entities, {
        root: "modules",
        jira: "modules/jira",
        confluence: "modules/confluence",
        fragment: "modules/fragment"
    });

    debug("\nEntity links:\n", util.inspect(entityLinks, {depth: 2}), "\n");

    var harpGlobals = fs.readJsonSync('./globals.json');

    harpGlobals.globals = _.extend({
        entityLinks: entityLinks,
        entities: entities,
        scopes: scopesView
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

    // debounce to prevent multiple rapid saves from kicking off multiple rebuilds
    restartHarpServer = _.debounce(restartHarpServer, 1000);

    harpServer = startHarpServer();

    var watchedFiles = srcFiles.concat(jiraSchemaSourcePath, confluenceSchemaSourcePath);

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

function compileJsDocs() {
    fork('./node_modules/.bin/jsdoc', ["-c", "jsdoc-conf.json", "-t", "jsdoc-template"]);
}

function dereferenceSchema() {
    var runOptions = [];
    if (program.debug)
        runOptions.push("-d");
    fork('./de-ref.js', runOptions)
}
rebuildHarpSite();

if (program.serve) {
    startHarpServerAndWatchSrcFiles()
} else {
    compileHarpSources();
}
