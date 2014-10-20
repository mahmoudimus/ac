/**
* Retrieve plugin versions from Manifesto and generate startup commands for JIRA/Confluence
* Then publish the commands in a file on DAC (Manifesto does not yet expose a public REST API, hence this workaround).
* Format:
* {
*   'commands' :
*     'jira': {
*       'dev': 'command'
*       'prd': 'command'
*     },
*     'confluence': {
*       'dev': 'command'
*       'prd': 'command'
*     }
* }
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
var outputFile = 'connect-versions';

var secret = argv.secret;

var manifestoBaseUrl = 'https://manifesto.uc-inf.net/api/env/jirastudio-';
var dacBaseUrl = 'https://developer.atlassian.com/static/';
var connectVersionsDACUrl = dacBaseUrl + outputFile + '.json';
var indexFile = outputFile + '-history.json';
var connectHistoryIndexUrl = dacBaseUrl +  historyFile;

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
    client.get(manifestoBaseUrl + 'prd', function (data, response) {
        console.log('retrieved info for production');
        data.details.products.forEach(function (product) {
            if (product.artifact == 'jira') {
                jiraVersion = product.version;
            }
            else if (product.artifact == 'confluence') {
                confluenceVersion = product.version;
            }
        });
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


        client.get(manifestoBaseUrl + 'dev', function (data, response) {
            console.log('retrieved info for dev');

            data.details.plugins.forEach(function (manifestoPlugin) {
                if (connectPluginName == manifestoPlugin.artifact) {
                    connectVersionDev = manifestoPlugin.version;
                }
            });

            createAndPublishCommands();
        });

    });
}


var commands = [];
var oldCommands;

function createAndPublishCommands() {
    var commands = createCommands();
	console.log('connecting to DAC');
	client.get(connectVersionsDACUrl, function (data, response) {
		console.log('retrieved current versions from DAC');
		var connect_dev = data.environment.dev.connectVersion;
		var confluence_dev = data.environment.dev.confluenceVersion;
		var jira_dev = data.environment.dev.jiraVersion;
		var connect_prd = data.environment.prd.connectVersion;
		var confluence_prd = data.environment.prd.confluenceVersion;
		var jira_prd = data.environment.prd.jiraVersion;
		
		if(commands.environment.dev.connectVersion != data.environment.dev.connectVersion
			|| commands.environment.dev.confluenceVersion != data.environment.dev.confluenceVersion
			|| commands.environment.dev.jiraVersion != data.environment.dev.jiraVersion
			|| commands.environment.prd.connectVersion != data.environment.prd.connectVersion
			|| commands.environment.prd.confluenceVersion != data.environment.prd.confluenceVersion
			|| commands.environment.prd.jiraVersion != data.environment.prd.jiraVersion) {
			
			console.log('New versions of the commands');	
			publishCommands(commands, oldCommands);
			
		} else {
			console.log('Commands are unchanged. Nothing to do. Just relax, you know, enjoy life a little.')
		}
	});
}

/**
 * Create the atlas-run-standalone commands
 */
function createCommands() {
	var validFrom = new Date();
    var commands =
    {
		'validFrom': validFrom.toDateString(),
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

    return commands;
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

function publishCommands(commands, oldCommands) {

    console.log('writing output file: ' + outputDir + '/' + outputFile);
    mkdirp(outputDir, function(err) {
		
		client.get(connectHistoryIndexUrl, function (data, response) {
			
			var index = data;
			var validTo = new Date();
			oldCommands.validTo = validTo.toDateString();
			var indexEntry = {};
			indexEntry.validFrom = oldCommands.validFrom;
			indexEntry.validTo = oldCommands.validTo;
			indexEntry.versionsFile = dacBaseUrl +
				outputFile + '-' validTo.getYear() + '-' + validTo.getMonth() + '-' +  validTo.getDate() + '.json';
			index.push(indexEntry);
			
    		var commandsJson = JSON.stringify(commands);
    		var commandsJsonFile = outputDir + '/' + outputFile + '.json';
   		 	var commandsJsonp = 'evalCommands('+commandsJson+')';
  		  	var commandsJsonpFile = outputDir + '/' + outputFile + '.jsonp';
			var oldCommandsJson = JSON.stringify(oldCommands);
			var oldCommandsJsonFile = outputDir + '/' + versionsFile;
			var indexJson = JSON.stringify(index);
			var indexJsonFile = outputDir + '/' + indexFile;
			
       	 	if(err) {
            	console.log('target directory ' + outputDir + ' could not be created: ' + err);
       	 	}
        	else {
            	fs.writeFile(commandsJsonFile, commandsJson, function(err) {
                	if(err)
                    	console.log('Error creating file ' + commandsJsonFile + ': ' + err);
                	else
                    	console.log('done');
            	});
            	fs.writeFile(commandsJsonpFile, commandsJsonp, function(err) {
                	if(err)
                    	console.log('Error creating file ' + commandsJsonpFile + ': ' + err);
                	else
                		console.log('done');
            	});
            	fs.writeFile(oldCommandsJsonFile, oldCommandsJson, function(err) {
                	if(err)
                    	console.log('Error creating file ' + oldCommandsJsonFile + ': ' + err);
                	else
                		console.log('done');
            	});
				
            	fs.writeFile(indexJsonFile, indexJson, function(err) {
                	if(err)
                    	console.log('Error creating file ' + indexJsonFile + ': ' + err);
                	else
                		console.log('done');
            	});
        	}
		});

    });
}


getPluginVersions();