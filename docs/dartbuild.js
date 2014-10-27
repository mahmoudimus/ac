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

function compileJsDocs() {
    fork('./node_modules/.bin/jsdoc', ["-c", "jsdoc-conf.json", "-t", "jsdoc-template"]);
}


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
    console.log('child process exited with code ' + code);

    harp = fork('./node_modules/harp/bin/harp', ["-o", "../www", "compile"], {'cwd': genSrcPrefix});

    harp.on('close', function (code) {
        console.log('harp child process exited with code ' + code);
    });

});