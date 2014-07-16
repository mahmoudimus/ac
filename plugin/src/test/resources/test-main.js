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
    'core-host': '../src/main/resources/js/core/host-debug',
    'aui-soy': '//aui-cdn.atlassian.com/aui-adg/5.4.3/js/aui-soy',
    // host side
    'confluence/macro/editor': '../src/main/resources/js/confluence/macro/editor',
    'jira/events': '../src/main/resources/js/jira/events',
    'jira/workflow-post-function': '../src/main/resources/js/jira/workflow-post-function/workflow-post-function',
    'iframe/host/main': '../src/main/resources/js/iframe/host/main'
  },

  shim: {
    /////////////////
    //  HOST SIDE  //
    /////////////////
    'confluence/macro/editor': {
      deps: ['core-host']
    },
    ///////////////////
    //  SHARED SIDE  //
    ///////////////////
    'iframe-plugin-confluence': {
        deps:[
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
