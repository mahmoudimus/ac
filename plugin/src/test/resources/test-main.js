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
    'jquery': '../target/qunit/dependencies/js/external/jquery/jquery',
    'aui-atlassian': '../target/qunit/dependencies/js/atlassian/atlassian',
    'iframe-host-ap': '../src/main/resources/js/iframe/host/_ap',
    'iframe-host-amd': '../src/main/resources/js/iframe/_amd',
    'iframe-host-dollar': '../src/main/resources/js/iframe/host/_dollar',
    'iframe-host-events': '../src/main/resources/js/iframe/_events',
    'iframe-host-xdm': '../src/main/resources/js/iframe/_xdm',
    'dialog-main': '../src/main/resources/js/dialog/main'
  },

  shim: {
    'aui-atlassian': {
      deps: [
        'jquery'
      ]
    },
    'iframe-host-amd': {
      deps: [
        'iframe-host-ap'
      ]
    },
    'iframe-host-dollar': {
      deps: [
        'jquery',
        'aui-atlassian',
        'iframe-host-amd'
      ]
    },
    'iframe-host-events': {
      deps: [
        'iframe-host-amd',
        'iframe-host-dollar'
      ]
    },
    'iframe-host-xdm': {
      deps: [
        'iframe-host-events',
        'iframe-host-dollar'
      ]
    },
    'dialog-main': {
      deps: [
        'iframe-host-dollar'
      ]
    }
  },

  // ask Require.js to load these files (all our tests)
  deps: tests,

  // start test run, once Require.js is done
  callback: window.__karma__.start
});
