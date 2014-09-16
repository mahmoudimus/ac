var helper = require('./bin-helper');

var atlassianConnectJsPath = 'node_modules/atlassian-connect-js';

helper.chain([
    [   // build distribution of connect javascsript
        helper.npmNormalize(atlassianConnectJsPath + '/node_modules/grunt-cli/bin/grunt'),
        ['--gruntfile', atlassianConnectJsPath + '/Gruntfile.js', 'build']
    ],
    [
        'cp',
        ['-a', helper.npmNormalize(atlassianConnectJsPath + '/dist/'), 'src/main/resources/js/core']
    ]

]);