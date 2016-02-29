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
    'aui-soy': '//aui-cdn.atlassian.com/aui-adg/5.4.3/js/aui-soy',
    'Squire' : '../node_modules/squirejs/src/Squire',
    'underscore': '../node_modules/underscore/underscore',
    // host side
    'connect-host': 'main/resources/js/core/connect-host',
    'ac/dialog': 'main/resources/js/core/connect-host-dialog',
    'ac/confluence/macro/editor': '../src/main/resources/js/confluence/macro/editor',
    'ac/confluence/macro/autoconvert': '../src/main/resources/js/confluence/macro/autoconvert',
    'ac/confluence/macro/property-panel-iframe': '../src/main/resources/js/confluence/macro/property-panel-iframe',
    'ac/confluence/macro/property-panel-controls': '../src/main/resources/js/confluence/macro/property-panel-controls',
    'ac/confluence/macro': '../src/main/resources/js/confluence/macro/macro',
    'ac/jira/events': '../src/main/resources/js/jira/events/events',
    'ac/jira/workflow-post-function': '../src/main/resources/js/jira/workflow-post-function/workflow-post-function'
  },

  shim: {
    /////////////////
    //  HOST SIDE  //
    /////////////////
    'underscore': {
      exports: '_'
    }
    ///////////////////
    //  SHARED SIDE  //
    ///////////////////
  }
});

require(tests, function() {
  window.__karma__.start();
});

//tests will timeout after 5 seconds
window.QUnit.config.testTimeout = 5000;
