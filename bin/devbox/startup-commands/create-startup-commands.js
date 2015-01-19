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

var client = new (require('node-rest-client').Client)(),
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
    '--version {{productVersion}} --bundled-plugins {{bundledPlugins}} ' +
    '--jvmargs -Datlassian.upm.on.demand=true';

var versionOverrides = {
    jira: {
        webhooks: '2.0.0'
    }
};

var versions = {};
var connectVersions = {};

var bundledPlugins = {
    'jira': [],
    'confluence': []
};

var _commands = [];
var _oldCommands= {};
var _index = [];

function getVersionsForEnv(opts, env, callback) {
	client.get(opts.manifestoBaseUrl + '/api/env/jirastudio-' + env, function (data, response) {
		opts.debug && console.log('retrieved info for ' + env);

		_.each(['jira', 'confluence'], function (product) {
			versions[product] = _.first(_.where(data.details.products, { artifact: product })).version;

			_.each(_.where(data.details.plugins, {product: product}), function (plugin) {
				var artifact = plugins[plugin.artifact];
				if (artifact) {
					bundledPlugins[product].push(artifact + ':' + plugin.version);
				} else if (plugin.artifact === 'atlassian-connect-plugin') {
					connectVersions[env] = plugin.version;
				}
			});
		});

		callback && callback();
	});
}

function createAndExportCommands(opts) {
   	createCommands();
	opts.debug && console.log('connecting to DAC');

	var dacConnectVersionsUrl = opts.dacBaseUrl + '/' + opts.outputFile + '.json';

	client.get(dacConnectVersionsUrl, function (data, response) {
		opts.debug && console.log('retrieved current versions from DAC');
		_oldCommands = data;
		
		if(newVersionsAvailable()) {
			opts.debug && console.log('New versions of the commands');
			exportCommands(opts);
		} else {
			opts.debug && console.log('Commands are unchanged. Nothing to do. Just relax, you know, enjoy life a little.')
		}
	});
}

function newVersionsAvailable() {
	return true;
	//return _commands.environment.dev.connectVersion != _oldCommands.environment.dev.connectVersion
	//	|| _commands.environment.dev.confluenceVersion != _oldCommands.environment.dev.confluenceVersion
	//	|| _commands.environment.dev.jiraVersion != _oldCommands.environment.dev.jiraVersion
	//	|| _commands.environment.prd.connectVersion != _oldCommands.environment.prd.connectVersion
	//	|| _commands.environment.prd.confluenceVersion != _oldCommands.environment.prd.confluenceVersion
	//	|| _commands.environment.prd.jiraVersion != _oldCommands.environment.prd.jiraVersion;
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
		'connectVersion': connectVersions[env],
		'jiraVersion' : versions.jira,
		'confluenceVersion' : versions.confluence,
		'jiraCommand': createCommand('jira', env),
		'confluenceCommand': createCommand('confluence', env)
	}
}

function createCommand(product, connectVersion) {
    return baseCommand
        .replace('{{product}}', product)
        .replace('{{productVersion}}', versions[product])
        .replace('{{bundledPlugins}}', bundledPlugins[product].join(',') + ',' + connectPlugin + ':' + connectVersion);
}

/**
 * Push the commands to Firebase
 * We use a token generated using the Firebase libraries. The token is only necessary to write the command
 * (read supports anonymous)
 */

function exportCommands(opts) {
    opts.debug && console.log('writing output file: ' + opts.outputDir + '/' + opts.outputFile);

	mkdirp(opts.outputDir, function(err) {
		if(err) {
        	console.log('target directory ' + outputDir + ' could not be created: ' + err);
   	 	} else {
			var indexFile = opts.outputFile + '-history.json';
			var versionsIndexUrl = opts.dacBaseUrl + indexFile;
			client.get(versionsIndexUrl, function (data, response) {
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
											('Error creating file ' + entry.file + ': ' + err) :
											('Wrote file: ' + entry.file)
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
			default: 'https://manifesto.uc-inf.net'
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
