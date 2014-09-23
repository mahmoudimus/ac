/**
* Retrieve plugin versions from Manifesto and generate startup commands for JIRA/Confluence
* Then publish the commands on Firebase (Manifesto does not yet expose a public REST API, hence this workaround).
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
* Firebase app: https://connect-utils.firebaseio.com
*/

var Client = require('node-rest-client').Client;
var Firebase = require("firebase");
var FirebaseTokenGenerator = require("firebase-token-generator");
var argv = require('yargs')
    .usage('Usage: $0 -url [url] -secret [string]')
    .demand(['url','secret'])
    .describe('url', 'URL of the firebase app (ending with firebaseio.com)')
    .describe('secret', 'Firebase secret, generated from the app')
    .argv;

var firebaseUrl = argv.url;
var secret = argv.secret;

var manifestoBaseUrl = 'https://manifesto.uc-inf.net/api/env/jirastudio-';
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


function run() {
    getPluginVersions();
}
run();

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
            if (product.artifact == 'confluence') {
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

function createAndPublishCommands() {
    var commands = createCommands();
    publishCommands(commands);
}

/**
 * Create the atlas-run-standalone commands
 */
function createCommands() {
    var commands =
    {
        'commands': {
            'jira': {
                'dev': createCommand('jira', jiraVersion, connectVersionDev),
                'prd': createCommand('jira', jiraVersion, connectVersionPrd)
            },
            'confluence': {
                'dev': createCommand('confluence', confluenceVersion, connectVersionDev),
                'prd': createCommand('confluence', confluenceVersion, connectVersionPrd)
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

function publishCommands(commands) {
    var firebaseRef = new Firebase(firebaseUrl);
    var tokenGenerator = new FirebaseTokenGenerator(secret);
    var token = tokenGenerator.createToken({uid: "1", loggedIn: true});
    firebaseRef.auth(token, function(error) {
        if(error) {
            console.log("Login Failed!", error);
        } else {
            console.log("Login Succeeded!");
        }
    });
    firebaseRef.set(commands, function (error) {
        if (error) {
            console.log("Data could not be saved." + error);
        } else {
            console.log("Data saved successfully.");
            process.exit(1);
        }
    });
}
