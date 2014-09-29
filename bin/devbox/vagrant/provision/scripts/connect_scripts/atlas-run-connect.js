var Client = require('node-rest-client').Client;
var sys = require('sys')
var exec = require('child_process').exec;
var argv = require('yargs')
    .usage('atlas-run-connect --product [product] --beta')
    .demand(['product'])
    .describe('product', 'jira/confluence')
    .describe ('beta', 'use the version of Connect in development (bleeding edge, may be unstable)')
    .argv;

var startupCommandsUrl = 'https://developer.atlassian.com/static/connect-versions.json';
var product = argv.product;
var beta = argv.beta;
var download = argv.downloadOnly;
var foo;
client = new Client();
var child;

client.get(startupCommandsUrl, function (data, response) {
    console.log('Downloaded versions file from ' + startupCommandsUrl);
    var command;
    var productVersion;
    var connectVersion;
    if(product == 'jira' && !beta) {
        command = data.environment.prd.jiraCommand;
        productVersion = data.environment.prd.jiraVersion;
        connectVersion = data.environment.prd.connectVersion;
    } else
    if(product == 'jira' && beta) {
        command = data.environment.dev.jiraCommand;
        productVersion = data.environment.dev.jiraVersion;
        connectVersion = data.environment.dev.connectVersion;
    } else
    if(product == 'confluence' && !beta) {
        command = data.environment.prd.confluenceCommand;
        productVersion = data.environment.prd.confluenceVersion;
        connectVersion = data.environment.prd.connectVersion;
    } else
    if(product == 'confluence' && beta) {
        command = data.environment.dev.confluenceCommand;
        productVersion = data.environment.dev.confluenceVersion;
        connectVersion = data.environment.dev.connectVersion;
    } else {
        console.log("Command not found. Exiting. ( woops, sorry :-/ )")
        process.exit(0);
    }

    if(command) {
        console.log("Starting local instance of "
            + product + " " + productVersion + " with Connect " + connectVersion);
        var commandName = 'atlas-run-standalone';
        command += " -DskipAllPrompts=true"
        var args = command.substr(commandName.length);
        run_cmd( commandName, args.split(' '));
    }

});

function run_cmd(cmd, args ) {
    var spawn = require('child_process').spawn;
    child = spawn(cmd, args);
    var resp = "";

    child.stdout.on('data', function (buffer) {
        var text = buffer.toString();
        process.stdout.write(text);
        if(download) {
            if (text.indexOf("starting...") > -1) {
                console.log("Finished downloading artifacts, terminating the process.")
                child.kill();
                process.exit(0);
            }

        }
    });
}