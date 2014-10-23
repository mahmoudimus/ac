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

var Client = require('node-rest-client').Client;
var fs = require('fs');
var mkdirp = require('mkdirp');
var argv = require('yargs')
    .usage('Usage: $0 --outputDir [path]')
    .demand(['outputDir'])
    .describe('outputDir', 'Target directory')
    .argv;

var outputDir = argv.outputDir;
var secret = argv.secret;

var OUTPUT_FILE = 'connect-versions';
var MANIFESTO_BASE_URL = 'https://manifesto.uc-inf.net/api/env/jirastudio-';
var DAC_BASE_URL = 'https://developer.atlassian.com/static/';
//var DAC_BASE_URL = 'http://localhost:5555/';
var CONNECT_VERSIONS_DAC_URL = DAC_BASE_URL + OUTPUT_FILE + '.json';
//an index file referencing old command files
var INDEX_FILE = OUTPUT_FILE + '-history.json';
var VERSIONS_INDEX_URL = DAC_BASE_URL +  INDEX_FILE;
//the maximum number of commands kept in the history
var MAX_HISTORY = 150;

var plugins = [
    'com.atlassian.jwt:jwt-plugin',
    'com.atlassian.bundles:json-schema-validator-atlassian-bundle',
    'com.atlassian.upm:atlassian-universal-plugin-manager-plugin',
    'com.atlassian.webhooks:atlassian-webhooks-plugin'
];
var connectPlugin = 'com.atlassian.plugins:atlassian-connect-plugin';
var baseCommand = 'atlas-run-standalone --product {{product}} ' +
    '--version {{productVersion}} --bundled-plugins {{bundledPlugins}} ' +
    '--jvmargs -Datlassian.upm.on.demand=true';


var jiraVersion;
var confluenceVersion;
var connectVersionPrd;
var connectVersionDev;
var bundledPluginsCmd = '';

/**
 * Retrieve plugin versions from Manifesto
 */
function getPluginVersions() {
    client = new Client();
    var connectPluginName = connectPlugin.split(':')[1];
    var processed = [];
    client.get(MANIFESTO_BASE_URL + 'prd', function (data, response) {
        console.log('retrieved info for production');
        data.details.products.forEach(function (product) {
            if (product.artifact == 'jira') {
                jiraVersion = product.version;
            }
            else if (product.artifact == 'confluence') {
                confluenceVersion = product.version;
            }
        })
		
        data.details.plugins.forEach(function (manifestoPlugin) {
            plugins.forEach(function (plugin) {
                var pluginName = plugin.split(':')[1];
                if (pluginName == manifestoPlugin.artifact) {
                    if (!processed[plugin]) {
                        if (bundledPluginsCmd != '')
                            bundledPluginsCmd += ","
                        bundledPluginsCmd += plugin + ":" + manifestoPlugin.version;
                        processed[plugin] = true;
                    }
                }

                if (connectPluginName == manifestoPlugin.artifact) {
                    connectVersionPrd = manifestoPlugin.version;
                }
            });
        });


        client.get(MANIFESTO_BASE_URL + 'dev', function (data, response) {
            console.log('retrieved info for dev');

            data.details.plugins.forEach(function (manifestoPlugin) {
                if (connectPluginName == manifestoPlugin.artifact) {
                    connectVersionDev = manifestoPlugin.version;
                }
            });

            createAndExportCommands();
        });

    });
}


var _commands = [];
var _oldCommands= {};
var _index = [];

function createAndExportCommands() {
   	createCommands();
	console.log('connecting to DAC');
	client.get(CONNECT_VERSIONS_DAC_URL, function (data, response) {
		console.log('retrieved current versions from DAC');
		_oldCommands = data;
		
		if(newVersionsAvailable()) {
			
			console.log('New versions of the commands');	
			exportCommands();
			
		} else {
			console.log('Commands are unchanged. Nothing to do. Just relax, you know, enjoy life a little.')
		}
	});
}

function newVersionsAvailable() {
	return _commands.environment.dev.connectVersion != _oldCommands.environment.dev.connectVersion
		|| _commands.environment.dev.confluenceVersion != _oldCommands.environment.dev.confluenceVersion
		|| _commands.environment.dev.jiraVersion != _oldCommands.environment.dev.jiraVersion
		|| _commands.environment.prd.connectVersion != _oldCommands.environment.prd.connectVersion
		|| _commands.environment.prd.confluenceVersion != _oldCommands.environment.prd.confluenceVersion
		|| _commands.environment.prd.jiraVersion != _oldCommands.environment.prd.jiraVersion;
}

/**
 * Create the atlas-run-standalone commands
 */
function createCommands() {
	var validFrom = new Date();
    _commands =
    {
		'validFrom': validFrom.toISOString(),
		'validTo' : 'OPEN',
        'environment': {
            'dev': {
                'connectVersion': connectVersionDev,
                'jiraVersion' : jiraVersion,
                'confluenceVersion' : confluenceVersion,
                'jiraCommand': createCommand('jira', jiraVersion, connectVersionDev),
                'confluenceCommand': createCommand('confluence', confluenceVersion, connectVersionDev)
            },
            'prd': {
                'connectVersion': connectVersionPrd,
                'jiraVersion' : jiraVersion,
                'confluenceVersion' : confluenceVersion,
                'jiraCommand': createCommand('jira', jiraVersion, connectVersionPrd),
                'confluenceCommand': createCommand('confluence', confluenceVersion, connectVersionPrd)
            }
        }
    }
}

function createCommand(product, productVersion, connectVersion) {
    return baseCommand
        .replace('{{product}}', product)
        .replace('{{productVersion}}', productVersion)
        .replace('{{bundledPlugins}}', bundledPluginsCmd + ',' + connectPlugin + ':' + connectVersion);
}

/**
 * Push the commands to Firebase
 * We use a token generated using the Firebase libraries. The token is only necessary to write the command
 * (read supports anonymous)
 */

function exportCommands() {

    console.log('writing output file: ' + outputDir + '/' + OUTPUT_FILE);
    mkdirp(outputDir, function(err) {
   	 	
		if(err) {
        	console.log('target directory ' + outputDir + ' could not be created: ' + err);
   	 	} else {
			client.get(VERSIONS_INDEX_URL, function (data, response) {
				if(response.statusCode != 404) {
					_index = data;
				}
				var validTo = new Date();
				_oldCommands.validTo = validTo.toISOString();
				var indexEntry = {};
				indexEntry.validFrom = _oldCommands.validFrom;
				indexEntry.validTo = _oldCommands.validTo;
				var versionsFile = OUTPUT_FILE + '_' + validTo.getTime() + '.json';
				indexEntry.versionsFile = DAC_BASE_URL + versionsFile;
		
				_index.unshift(indexEntry);
				while(_index.length > MAX_HISTORY) {
					_index.pop();
				}

				var files = 
				[
					{
						'content': JSON.stringify(_commands), 
						'file': outputDir + '/' + OUTPUT_FILE + '.json'
					},
					{
						'content': 'evalCommands(' + JSON.stringify(_commands) + ')', 
						'file': outputDir + '/' + OUTPUT_FILE + '.jsonp'
					},
					{
						'content': JSON.stringify(_oldCommands), 
						'file': outputDir + '/' + versionsFile
					},
					{
						'content': JSON.stringify(_index), 
						'file': outputDir + '/' + INDEX_FILE
					}
				];

				files.forEach(function(entry) {
					fs.writeFile(entry.file, entry.content, function(err) {
			    		if(err)
			        		console.log('Error creating file ' + entry.file + ': ' + err);
			    		else
			        		console.log('Wrote file: ' + entry.file);
					});
				});
    		});
		}
	});
}


getPluginVersions();