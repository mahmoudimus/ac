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
    // host side
    'connect-host': 'main/resources/js/core/connect-host',
    'ac/dialog': 'main/resources/js/core/connect-host-dialog',
    'ac/confluence/macro/editor': '../src/main/resources/js/confluence/macro/editor',
    'ac/jira/events': '../src/main/resources/js/jira/events/events',
    'ac/jira/workflow-post-function': '../src/main/resources/js/jira/workflow-post-function/workflow-post-function'
  },

  shim: {
    /////////////////
    //  HOST SIDE  //
    /////////////////
    ///////////////////
    //  SHARED SIDE  //
    ///////////////////
  },

  // ask Require.js to load these files (all our tests)
  deps: tests,

    // start test run, once Require.js is done
    callback: function(x){ 
        setTimeout(function(){
             window.__karma__.start(x);
        }, 1000);
    }
});

//tests will timeout after 5 seconds
window.QUnit.config.testTimeout = 5000;
