var spawn = require('child_process').spawn;
var normalizePath = require('path').normalize;
var isWin = require('os').platform() === 'win32';

exports.chain = function chain(commands) {
    if (commands.length) {
        var cmd = commands.shift();
        var opts;
        if(cmd.length > 2 || !(cmd[1] instanceof Array)) {
            opts = cmd[cmd.length - 1];
        } else {
            cmd.push(opts = {});
        }
        opts.stdio = 'inherit';
        opts.env = process.env;

        var args = cmd[1] && cmd[1].join && cmd[1].join(' ') || '';
        console.log('Running ' + cmd[0] + ' ' + args);

        var proc = spawn.apply(null, cmd);
        proc.on('error', console.log.bind(console, cmd));
        proc.on('close', chain.bind(null, commands));
    }
};

exports.pathNormalize = normalizePath;
exports.npmNormalize = function(str) {
    return normalizePath(str) + (isWin ? '.cmd' : '');
};