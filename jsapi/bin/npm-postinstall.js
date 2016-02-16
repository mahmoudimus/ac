var helper = require('./bin-helper');

var atlassianConnectJsPath = 'node_modules/atlassian-connect-js';
var simpleXdmJsPath = 'node_modules/simple-xdm';
var atlassianConnectJsCookiePath = "node_modules/atlassian-connect-js-cookie";
var atlassianConnectJsHistoryPath = "node_modules/atlassian-connect-js-history";
var atlassianConnectJsRequestPath = "node_modules/atlassian-connect-js-request";

helper.chain([
    [
        'cp',
        ['-af', helper.npmNormalize(atlassianConnectJsPath + '/dist') + '/.', 'src/main/resources/js/core']
    ],
    [
        'cp',
        ['-af', helper.npmNormalize(atlassianConnectJsCookiePath + '/dist') + '/connect-host-cookie.js', 'src/main/resources/js/core']
    ],
    [
        'cp',
        ['-af', helper.npmNormalize(atlassianConnectJsHistoryPath + '/dist') + '/.', 'src/main/resources/js/core']
    ],
    [
        'cp',
        ['-af', helper.npmNormalize(atlassianConnectJsRequestPath + '/dist') + '/.', 'src/main/resources/js/core']
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
    ],
    [
        'sed',
        ['-i', "", 's/define(\\[/define("connect-host-cookie",\\[/g', "src/main/resources/js/core/connect-host-cookie.js"]
    ]


]);