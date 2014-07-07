var helper = require('./bin-helper');

var atlassianConnectJsPath = 'node_modules/atlassian-connect-js';

helper.chain([
    [   // build distribution of connect javascsript
        'grunt',
        ['--gruntfile', atlassianConnectJsPath + '/Gruntfile.js', 'build']
    ],
    [
        'cp',
        ['-R', helper.npmNormalize(atlassianConnectJsPath + '/dist'), 'src/main/resources/js/core']
    ]

]);