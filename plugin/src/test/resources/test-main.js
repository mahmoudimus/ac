var tests = [];
for (var file in window.__karma__.files) {
  if (window.__karma__.files.hasOwnProperty(file)) {
    if (/-test\.js$/.test(file)) {
      tests.push(file);
    }
  }
}

requirejs.config({
  // Karma serves files from '/base'
  baseUrl: '/base/src',

  paths: {
    // dependencies
    'jquery': '../target/qunit/dependencies/js/external/jquery/jquery',
    'aui-atlassian': '../target/qunit/dependencies/js/atlassian/atlassian',
    'aui-soy': '//aui-cdn.atlassian.com/aui-adg/5.4.3/js/aui-soy',
    // host side
    'iframe/host/_ap': '../src/main/resources/js/iframe/host/_ap',
    'iframe/host/_status_helper': '../src/main/resources/js/iframe/host/_status_helper',
    'iframe/host/_dollar': '../src/main/resources/js/iframe/host/_dollar',
    'iframe/host/content': '../src/main/resources/js/iframe/host/content',
    'dialog/main': '../src/main/resources/js/dialog/main',
    'dialog/button': '../src/main/resources/js/dialog/button',
    'dialog/dialog-factory': '../src/main/resources/js/dialog/dialog-factory',
    'inline-dialog/main': '../src/main/resources/js/inline-dialog/main',
    'inline-dialog/simple': '../src/main/resources/js/inline-dialog/simple',
    'confluence/macro/editor': '../src/main/resources/js/confluence/macro/editor',
    'jira/event': '../src/main/resources/js/jira/event',
    'messages/main': '../src/main/resources/js/messages/main',
    // shared
    'iframe/_amd': '../src/main/resources/js/iframe/_amd',
    'iframe/_events': '../src/main/resources/js/iframe/_events',
    'iframe/_xdm': '../src/main/resources/js/iframe/_xdm',
    'iframe/_uri': '../src/main/resources/js/iframe/_uri',
    'iframe/_base64': '../src/main/resources/js/iframe/_base64',
    'iframe/_ui-params': '../src/main/resources/js/iframe/_ui-params',
    'iframe/host/main': '../src/main/resources/js/iframe/host/main'
  },

  shim: {
    /////////////////
    //  HOST SIDE  //
    /////////////////
    'aui-atlassian': {
      deps: [
        'jquery'
      ]
    },
    'iframe/host/_dollar': {
      deps: [
        'jquery',
        'aui-atlassian',
        'iframe/_amd'
      ]
    },
    'iframe/host/content': {
        deps: [
        'jquery',
        'aui-atlassian',
        'iframe/_amd',
        'iframe/_ui-params'
        ]
    },
    'iframe/_base64': {
      deps: [
        'iframe/host/_dollar',
        'iframe/_amd',
      ]
    },
    'inline-dialog/simple': {
      deps: [
        'iframe/host/_dollar',
        'iframe/host/content',
        'iframe/host/_status_helper',
        'iframe/_ui-params'
      ]
    },
    'iframe/host/_status_helper': {
      deps: [
          'iframe/host/_dollar'
      ]
    },
    'inline-dialog/main': {
      deps: [
        'iframe/host/_dollar',
        'iframe/host/content',
        'iframe/host/_status_helper'
      ]
    },
    'dialog/main': {
      deps: [
        'iframe/host/_dollar',
        'iframe/_ui-params',
        'iframe/host/_status_helper',
        'dialog/button',
        'aui-soy'
      ]
    },
    'dialog/button': {
      deps: [
      'iframe/host/_dollar'
      ]
    },
    'dialog/dialog-factory': {
      deps: [
      'iframe/host/_dollar',
      'dialog/main'
      ]
    },
    'confluence/macro/editor': {
        deps: [
        'iframe/host/_dollar',
        'dialog/main'
        ]
    },
    'jira/event': {
        deps: [
        'iframe/host/_dollar'
        ]
    },
    'messages/main': {
        deps: [
        'iframe/host/_dollar'
        ]
    },
    ///////////////////
    //  SHARED SIDE  //
    ///////////////////
    'iframe/_amd': {
      deps: [
        'iframe/host/_ap'
      ]
    },
    'iframe/_events': {
      deps: [
        'iframe/_amd'
      ]
    },
    'iframe/_uri': {
      deps: [
      'iframe/_amd'
      ]
    },
    'iframe/_ui-params': {
      deps: [
        'iframe/host/_dollar',
        'iframe/_uri',
        'iframe/_base64'
      ]
    },
    'iframe/_xdm': {
      deps: [
        'iframe/_uri',
        'iframe/_events'
      ]
    },
    'iframe/host/main':{
        deps: [
        'iframe/host/_ap',
        'iframe/host/_dollar',
        'iframe/_amd'
        ]
    },
    'iframe-plugin-confluence': {
        deps:[
        'iframe/_amd',
        'iframe/host/_dollar',
        'iframe/host/main'
        ]
    }
  },

  // ask Require.js to load these files (all our tests)
  deps: tests,

  // start test run, once Require.js is done
  callback: window.__karma__.start
});

//tests will timeout after 5 seconds
window.QUnit.config.testTimeout = 5000;
