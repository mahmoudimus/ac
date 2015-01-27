#!/usr/bin/env node

var request = require('request'),
        fs = require('fs'),
        exec = require('child_process').exec,
        spawn = require('child_process').spawn,
        extend = require('node.extend'),
        nomnom = require('nomnom'),
        colors = require('colors'),
        util = require('util');

var selfExecute = require.main === module;

function doRun(opts) {
    request.get({
        url: opts.startupCommandsUrl,
        json: true
    }, function (error, response, data) {
        if (error) {
            console.log('ERROR'.red, 'Could not download versions file from: ' + opts.startupCommandsUrl);
            console.log('ERROR'.red, 'Using cached copy: ' + opts.commandsCacheFile);
            useCachedCommands(opts);
        } else {
            console.log('Info'.yellow, 'Downloaded versions file from: ' + opts.startupCommandsUrl);
            opts.debug && console.log(data);
            cacheCommands(opts, data);
            runCommand(opts, data);
        }
    })
}

function runCommand(opts, data) {
    var product = opts.product,
        beta = opts.beta;
    var command;
    var productVersion;
    var connectVersion;
    if(product == 'jira' && !beta) {
        command = data.environment.prd.jiraCommand;
        productVersion = data.environment.prd.jiraVersion;
        connectVersion = data.environment.prd.connectVersion;
    } else if(product == 'jira' && beta) {
        command = data.environment.dev.jiraCommand;
        productVersion = data.environment.dev.jiraVersion;
        connectVersion = data.environment.dev.connectVersion;
    } else if(product == 'confluence' && !beta) {
        command = data.environment.prd.confluenceCommand;
        productVersion = data.environment.prd.confluenceVersion;
        connectVersion = data.environment.prd.connectVersion;
    } else if(product == 'confluence' && beta) {
        command = data.environment.dev.confluenceCommand;
        productVersion = data.environment.dev.confluenceVersion;
        connectVersion = data.environment.dev.connectVersion;
    } else {
        console.log("Error".red, "Command not found for product " + product + ".");
        process.exit(0);
    }

    if(command) {
        console.log("Starting local instance of " + product + " " + productVersion + " with Connect " + connectVersion);
        var commandName = 'atlas-run-standalone';
        command += " -DskipAllPrompts=true";
        var args = command.substr(commandName.length);
        run_cmd(commandName, args.split(' '));
    }
}

function cacheCommands(opts, data) {
    fs.writeFile(opts.commandsCacheFile, JSON.stringify(data), function(err) {
        if(err) {
            console.log("Error".red, "Couldn't save commands in file: " + opts.commandsCacheFile);
        } else {
            console.log("Info".yellow, "Cached commands in file: " + opts.commandsCacheFile);
        }
    });
}

function useCachedCommands(opts) {
    fs.readFile(opts.commandsCacheFile, function(err, data) {
        if(err) {
            console.log("Error".red, "Couldn't find local commands file: " + opts.commandsCacheFile);
            console.log("Is it the first time you have run this command? Make sure you are connected to the internet!")
        } else {
            console.log("Warn".yellow, "Using the latest cached command - this may not be the version currently in production!");
            runCommand(JSON.parse(data));
        }
    });
}

var child;

function run_cmd(cmd, args) {
    child = spawn(cmd, args);

    child.stdout.on('data', function (buffer) {
        var text = buffer.toString();
        process.stdout.write(text);
        if(text.indexOf("==> default: [INFO] BUILD FAILURE") > -1) {
            killProcess(1);
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
        .option('product', {
            abbr: 'p',
            required: true,
            choices: ['jira', 'confluence']
        })
        .option('beta', {
            flag: true,
            default: false,
            help: 'Use the version of Connect in development (bleeding edge, may be unstable)'
        })
        .option('startupCommandsUrl', {
            default: 'https://developer.atlassian.com/static/connect/commands/connect-versions.json'
        })
        .option('commandsCacheFile', {
            default: '/home/vagrant/scripts/cache/commands.json'
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

    doRun(opts);
};

if (selfExecute) {
    exports.run();
}


function killProcess(code) {
    child && child.kill();
    process.exit(code);
}
