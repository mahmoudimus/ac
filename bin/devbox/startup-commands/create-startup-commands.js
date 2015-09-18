#!/usr/bin/env node

/**
* Retrieve plugin versions from Manifesto and generate startup commands for JIRA/Confluence
* Then publish the commands in a file on DAC (Manifesto does not yet expose a public REST API, hence this workaround).
* 
* Save older versions of the commands.
* 
* Note: the only difference between the commands for dev and prod is the version of the Connect plugin.
* Both the prod and dev commands use the production version of JIRA and Confluence, as well as other
* plugin dependencies.
*
*/

var request = require('request'),
        fs = require('fs'),
        _ = require('lodash'),
        async = require('async'),
        extend = require('node.extend'),
        nomnom = require('nomnom'),
        mkdirp = require('mkdirp'),
        colors = require('colors'),
        util = require('util');

var selfExecute = require.main === module;

var downloadQueue = async.queue(function (task, callback) {
    var args = task.args;
    args.push(callback);
    task.executor.apply(task.self || this, args);
}, 3);

var plugins = {
    'jwt-plugin': 'com.atlassian.jwt:jwt-plugin',
    'json-schema-validator-atlassian-bundle': 'com.atlassian.bundles:json-schema-validator-atlassian-bundle',
    'atlassian-universal-plugin-manager-plugin': 'com.atlassian.upm:atlassian-universal-plugin-manager-plugin',
    'atlassian-webhooks-plugin': 'com.atlassian.webhooks:atlassian-webhooks-plugin'
};

var connectPlugin = 'com.atlassian.plugins:atlassian-connect-plugin';
var baseCommand = 'atlas-run-standalone --product {{product}} ' +
    '--version {{productVersion}} --data-version {{dataVersion}} --bundled-plugins {{bundledPlugins}} ' +
    '--jvmargs -Datlassian.upm.on.demand=true';

var versionOverrides = {
    'prd': {
        jira: {
            'atlassian-webhooks-plugin': '2.0.0'
        }
    },
    'dev': {
        jira: {
            'atlassian-webhooks-plugin': '2.0.0'
        }
    }
};

var versions = {
    'prd': {},
    'dev': {}
};

var bundledPlugins = {
    'prd': {
        'jira': [],
        'confluence': []
    },
    'dev': {
        'jira': [],
        'confluence': []
    }
};

var _commands = [];
var _oldCommands= {};
var _index = [];

function getVersionsForEnv(opts, env, callback) {
    request.get({
        url: opts.manifestoBaseUrl + '/api/env/jirastudio-' + env,
        json: true
    }, function (error, response, data) {
        opts.debug && console.log('Info'.yellow, 'Retrieved manifesto versions for ' + env.blue);

        _.each(['jira', 'confluence'], function (product) {
            versions[env][product] = _.first(_.where(data.details.products, { artifact: product })).version;

            _.each(_.where(data.details.plugins, {product: product}), function (plugin) {
                var artifact = plugins[plugin.artifact];
                if (artifact) {
                    var v = plugin.version;
                    var envOverride = versionOverrides[env];
                    var productOverride = envOverride && envOverride[product];
                    var pluginVersionOverride = productOverride && productOverride[plugin.artifact];
                    if (pluginVersionOverride) {
                        opts.debug && console.log("Version override".red, ("[" + product + ":" + env + "]").grey, artifact, ("" + v).yellow, '-->', pluginVersionOverride.green);
                        v = pluginVersionOverride;
                    }
                    bundledPlugins[env][product].push(artifact + ':' + v);
                } else if (plugin.artifact === 'atlassian-connect-plugin') {
                    versions[env].connect = plugin.version;
                }
            });
        });

        callback && callback();
    });
}

function createAndExportCommands(opts) {
    createCommands();
    opts.debug && console.log('Info'.yellow, 'Retrieving versions from DAC');

    request.get({
        url: opts.dacBaseUrl + '/connect/commands/' + opts.outputFile + '.json',
        json: true
    }, function (error, response, data) {
        if (error) {
            console.log(error);
        } else {
            _oldCommands = data;

            if (newVersionsAvailable(opts)) {
                opts.debug && console.log('Info'.blue, 'New versions available, regenerating commands');
                exportCommands(opts);
            }
        }
    });
}

function newVersionsAvailable(opts) {
    if (!_oldCommands.environment) {
        opts.debug && console.log('Warn'.yellow, 'No old commands available');
        return true;
    }
    return _commands.environment.dev.jiraCommand != _oldCommands.environment.dev.jiraCommand
            || _commands.environment.dev.confluenceCommand != _oldCommands.environment.dev.confluenceCommand
            || _commands.environment.prd.jiraCommand != _oldCommands.environment.prd.jiraCommand
            || _commands.environment.prd.confluenceCommand != _oldCommands.environment.prd.confluenceCommand;
}

/**
 * Create the atlas-run-standalone commands
 */
function createCommands(opts) {
    var validFrom = new Date();
    _commands =
    {
        'validFrom': validFrom.toISOString(),
        'validTo' : 'OPEN',
        'environment': {
            'dev': createEnvironment('dev'),
            'prd': createEnvironment('prd')
        }
    }
}

function createEnvironment(env) {
    return {
        'connectVersion': versions[env].connect,
        'jiraVersion' : versions.jira,
        'confluenceVersion' : versions.confluence,
        'jiraCommand': createProductRunCommand('jira', env),
        'confluenceCommand': createProductRunCommand('confluence', env)
    }
}

function createProductRunCommand(product, env) {
    return baseCommand
        .replace('{{product}}', product)
        .replace('{{productVersion}}', versions[env][product])
        .replace('{{dataVersion}}', versions[env][product])
        .replace('{{bundledPlugins}}', bundledPlugins[env][product].join(',') + ',' + connectPlugin + ':' + versions[env].connect);
}

/**
 * Push the commands to Firebase
 * We use a token generated using the Firebase libraries. The token is only necessary to write the command
 * (read supports anonymous)
 */

function exportCommands(opts) {
    mkdirp(opts.outputDir, function(err) {
        if(err) {
            console.log('Error'.red, ' Target directory ' + opts.outputDir + ' could not be created: ' + err);
        } else {
            var indexFile = opts.outputFile + '-history.json';
            request.get({
                url: opts.dacBaseUrl + indexFile,
                json: true
            }, function (error, response, data) {
                if (error) {
                    console.log(error);
                    return;
                }
                if(response.statusCode != 404) {
                    _index = data;
                }
                var validTo = new Date();
                _oldCommands.validTo = validTo.toISOString();
                var indexEntry = {};
                indexEntry.validFrom = _oldCommands.validFrom;
                indexEntry.validTo = _oldCommands.validTo;
                var versionsFile = opts.outputFile + '_' + validTo.getTime() + '.json';
                indexEntry.versionsFile = opts.dacBaseUrl + '/' + versionsFile;
        
                _index.unshift(indexEntry);
                while(_index.length > opts.maxHistory) {
                    _index.pop();
                }

                var files = 
                [
                    {
                        'content': JSON.stringify(_commands), 
                        'file': opts.outputDir + '/' + opts.outputFile + '.json'
                    },
                    {
                        'content': 'evalCommands(' + JSON.stringify(_commands) + ')', 
                        'file': opts.outputDir + '/' + opts.outputFile + '.jsonp'
                    },
                    {
                        'content': JSON.stringify(_oldCommands), 
                        'file': opts.outputDir + '/' + versionsFile
                    },
                    {
                        'content': JSON.stringify(_index), 
                        'file': opts.outputDir + '/' + indexFile
                    }
                ];

                files.forEach(function(entry) {
                    fs.writeFile(entry.file, entry.content, function(err) {
                        if (opts.debug) {
                            console.log(
                                    err ?
                                            ('Error'.red + ' Could not write file ' + entry.file + ': ' + err) :
                                            ('Saved'.green + ' ' + entry.file)
                            );
                        }
                    });
                });
            });
        }
    });
}

var getCliOpts = function(callback) {
    var nn = nomnom
        .option('debug', {
            abbr: 'd',
            flag: true,
            help: 'Print debugging info'
        })
        .option('outputDir', {
            abbr: 'o',
            default: 'target',
            required: true,
            help: 'Output directory'
        })
        .option('manifestoBaseUrl', {
            default: 'https://manifesto.atlassian.io'
        })
        .option('dacBaseUrl', {
            default: 'https://developer.atlassian.com/static'
        })
        .option('outputFile', {
            default: 'connect-versions'
        })
        .option('maxHistory', {
            default: 150
        });

    if (callback) {
        nn = callback(nn);
    }
    return nn;
};

exports.run = function(runOpts) {
    var nomnom = getCliOpts(runOpts ? runOpts.cliOptsCallback : false);

    var opts = runOpts;
    opts = extend(opts, nomnom.parse());

    downloadQueue.push({
        self: this,
        executor: getVersionsForEnv,
        args: [opts, 'dev']
    }, function () { });

    downloadQueue.push({
        self: this,
        executor: getVersionsForEnv,
        args: [opts, 'prd']
    }, function () { });

    downloadQueue.drain = function () {
        createAndExportCommands(opts);
    };
};

if (selfExecute) {
    exports.run();
}
