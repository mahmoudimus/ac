var helper = require('./bin-helper');

var atlassianConnectJsPath = 'node_modules/atlassian-connect-js';
var simpleXdmJsPath = 'node_modules/simple-xdm';

helper.chain([
    [
        'cp',
        ['-a', helper.npmNormalize(atlassianConnectJsPath + '/dist') + '/.', 'src/main/resources/js/core']
    ],
    [
        'cp',
        ['-a', helper.npmNormalize(simpleXdmJsPath + '/dist') + '/iframe.js', 'src/main/resources/js/core/iframe.js']
    ],
    [
        'mkdir',
        ['src/main/resources/css/core']
    ],
    [
        'cp',
        ['-a', helper.npmNormalize(atlassianConnectJsPath + '/dist/host-css.css'), 'src/main/resources/css/core/host-css.css']
    ]


]);