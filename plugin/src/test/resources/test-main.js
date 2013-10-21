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
    // host side
    'iframe/host/_ap': '../src/main/resources/js/iframe/host/_ap',
    'iframe/host/_dollar': '../src/main/resources/js/iframe/host/_dollar',
    'dialog/main': '../src/main/resources/js/dialog/main',
    // shared
    'iframe/_amd': '../src/main/resources/js/iframe/_amd',
    'iframe/_events': '../src/main/resources/js/iframe/_events',
    'iframe/_xdm': '../src/main/resources/js/iframe/_xdm',
    'iframe-host-main': '../src/main/resources/js/iframe/host/main',
    'iframe-host-resize_listener': '../src/main/resources/js/iframe/plugin/_resize_listener',
    'iframe-plugin-confluence': '../src/main/resources/js/confluence/macro/editor'
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
    'dialog/main': {
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
    'iframe/_xdm': {
      deps: [
        'iframe/_events'
      ]
    },
    'iframe-host-main':{
        deps: [
        'iframe-host-ap',
        'iframe-host-amd',
        'iframe-host-dollar'
        ]
    },
    'iframe-host-resize_listener': {
        deps: [
        'iframe-host-amd',
        'iframe-host-dollar',
        'iframe-host-main',
        ]
    },
    'iframe-plugin-confluence': {
        deps:[
        'iframe-host-amd',
        'iframe-host-dollar',
        'iframe-host-main',
        ]
    }
  },

  // ask Require.js to load these files (all our tests)
  deps: tests,

  // start test run, once Require.js is done
  callback: window.__karma__.start
});
