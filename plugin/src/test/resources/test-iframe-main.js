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
    baseUrl: 'base/src/main/resources/js/iframe/plugin',

    paths: {
        'iframe/_events': '../_events',
        'iframe/_uri': '../_uri',
        '_events': '../_events',
        'iframe/_ui-params': '../_ui-params',
    },
    // ask Require.js to load these files (all our tests)
    deps: tests,

    // start test run, once Require.js is done
    callback: window.__karma__.start
});

//tests will timeout after 5 seconds
window.QUnit.config.testTimeout = 5000;
