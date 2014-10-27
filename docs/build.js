#!/usr/bin/env node

// TODO this file should be refactored out into a couple of libraries to separate the concerns of schema munging, static directory construction and harp server lifecycle

var _ = require('lodash');
var fs = require('fs-extra');
var util = require('util');
var renderMarkdown = require("markdown-js").markdown;
var proc = require("child_process");
var spawn = proc.spawn;
var fork = proc.fork;
var chokidar = require("chokidar");
var jsonPath = require("JSONPath").eval;
var program = require("commander");
var dereferencer = require("./de-ref");

var buildDir = "./target";
var genSrcPrefix = buildDir + "/gensrc";

var srcFiles = ["public", "package.json"];

var jiraSchemaSourcePath =       '../plugin/target/classes/schema/jira-schema.json';
var confluenceSchemaSourcePath = '../plugin/target/classes/schema/confluence-schema.json';

var schemaDirPath =       'schema';

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
 * Copy the supplied files to the gensrc directory.
 */
function copyToGenSrc(filenames) {
    if (typeof filenames === "string") filenames = [filenames];
    _.each(filenames, function (filename) {
        fs.copySync(filename, genSrcPrefix + '/' + filename);
    });
}





/**
 * Delete the build dir, regenerate the model from the schema and rebuild the documentation.
 */
function rebuildHarpSite(done, error) {
    fs.deleteSync(buildDir);

    fs.mkdirsSync(genSrcPrefix);

    copyToGenSrc(srcFiles);
    copyToGenSrc("node_modules");

    dereferencer.run();

    compileJsDocs();

    console.log('jiraSchemaPath: ' + fs.realpathSync(schemaDirPath));

    dart = spawn('dart', ['dartdoc/bin/main.dart',
        fs.realpathSync(schemaDirPath), fs.realpathSync(genSrcPrefix)]);

    dart.stdout.on('data', function (data) {
        console.log('stdout: ' + data);
    });

    dart.stderr.on('data', function (data) {
        console.log('stderr: ' + data);
    });

    dart.on('close', function (code) {
        console.log('doc generator process exited with code ' + code);
        if (code == 0) {
            done();
        }
        else {
            console.log('Failed to generate docs from schemas');
            if (error) {
                error();
            }
        }

    });
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
            rebuildHarpSite(function() {
                harpServer = startHarpServer();
                restarting = false;
            });
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

rebuildHarpSite(
    function() {
        if (program.serve) {
            startHarpServerAndWatchSrcFiles()
        } else {
            compileHarpSources();
        }
    },
    function() {
        if (!program.serve) {
            process.exit(1);
        }
    }
);

