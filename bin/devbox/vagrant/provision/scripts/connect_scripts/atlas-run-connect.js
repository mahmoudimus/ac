var Client = require('node-rest-client').Client;
var fs = require('fs');
var sys = require('sys')
var exec = require('child_process').exec;
var argv = require('yargs')
    .usage('atlas-run-connect --product [product] --beta')
    .demand(['product'])
    .describe('product', 'jira/confluence')
    .describe ('beta', 'use the version of Connect in development (bleeding edge, may be unstable)')
    .argv;

var commandsCacheFile = '/home/vagrant/scripts/cache/commands.json';
var startupCommandsUrl = 'https://developer.atlassian.com/static/connect/commands/connect-versions.json';
var product = argv.product;
var beta = argv.beta;
var download = argv.downloadOnly;
var foo;
client = new Client();
var child;


client.registerMethod("jsonMethod", startupCommandsUrl, "GET");

var req=client.methods.jsonMethod(function (data, response) {
    console.log('Downloaded versions file from ' + startupCommandsUrl);
    cacheCommands(data);
    runCommand(data);

});


req.on('error',function(err){
    console.log('Could not download versions file from: ' + startupCommandsUrl);
    useCachedCommands();
});

function runCommand(data) {
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
        console.log("Command not found for product " + product + ". Exiting.")
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
}

function cacheCommands(data) {
    fs.writeFile(commandsCacheFile, JSON.stringify(data), function(err) {
        if(err) {
            console.log("WARN: couldn't save commands in file: " + commandsCacheFile);
        } else {
            console.log("Cached commands in file: " + commandsCacheFile);
        }
    });
}

function useCachedCommands() {
    fs.readFile(commandsCacheFile, function(err, data) {
        if(err) {
            console.log("ERROR: Couldn't find local commands file: " + commandsCacheFile);
            console.log("Is it the first time you try to run this command? Make sure you are connected to the internet!")
        } else {
            console.log("WARN: using the latest cached command - this may not be the version currently in production!");
            runCommand(JSON.parse(data));
        }
    });
}

function run_cmd(cmd, args ) {
    var spawn = require('child_process').spawn;
    child = spawn(cmd, args);
    var resp = "";

    child.stdout.on('data', function (buffer) {
        var text = buffer.toString();
        process.stdout.write(text);
        if(text.indexOf("==> default: [INFO] BUILD FAILURE") > -1) {
            killProcess(1);
        }
        if(download) {
            if (text.indexOf("starting...") > -1) {
                console.log("Finished downloading artifacts, terminating the process.");
                killProcess(0);
            }
        }
    });
}

function killProcess(code) {
    child.kill();
    process.exit(code);
}