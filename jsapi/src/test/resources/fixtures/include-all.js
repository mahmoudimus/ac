require.config({
    baseUrl: '/base/src/main/resources/js/iframe/',
     // Use shim for plugins that does not support ADM
    shim: {
        'plugin/_util': {
            deps: [
                '_amd'
            ]
        },
        'plugin/_dollar': {
            deps: [
                '_amd',
                'plugin/_util'
            ]
        },
        '_events': {
            deps: [
                '_amd'
            ]
        },
        '_xdm': {
            deps: [
                '_events'
            ]
        },
        'plugin/_rpc': {
            deps: [
                '_xdm'
            ]
        },
        'plugin/events': {
            deps: [
                'plugin/_rpc'
            ]
        },
        'plugin/env': {
            deps: [
                'plugin/events'
            ]
        },
        'plugin/request': {
            deps: [
                'plugin/env'
            ]
        },
        'plugin/dialog': {
            deps: [
                'plugin/request'
            ]
        },
        'plugin/_resize_listener': {
            deps: [
                'plugin/dialog'
            ]
        },
        'plugin/jira': {
            deps: [
                'plugin/_resize_listener'
            ]
        },
        'plugin/confluence': {
            deps: [
                'plugin/jira'
            ]
        },
        'plugin/_init': {
            deps: [
                '_amd',
                'plugin/_util',
                'plugin/_dollar',
                '_events',
                '_xdm',
                'plugin/_rpc',
                'plugin/events',
                'plugin/env',
                'plugin/request',
                'plugin/dialog',
                'plugin/_resize_listener',
                'plugin/jira',
                'plugin/confluence'
                ]
        },
    }
});

require([
    '//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js',
    'plugin/_init'
    ], function(){
        window.requireLoaded();
    });