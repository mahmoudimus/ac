// Karma configuration
// Generated on Wed Oct 16 2013 15:12:27 GMT+1100 (EST)

module.exports = function(config) {
  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '',


    // frameworks to use
    frameworks: ['qunit', 'requirejs', 'sinon'],


    // list of files / patterns to load in the browser
    files: [
      'src/test/resources/test-iframe-main.js',
      {pattern: 'node_modules/sinon/lib/sinon/util/timers_ie.js', included: true},
      'src/test/resources/js/iframe/plugin/fixture.js',
      {pattern: 'src/test/resources/js/iframe/plugin/*-test.js', included: false},
      {pattern: 'src/test/resources/js/iframe/plugin/_*Mock*.js', included: false},
      {pattern: 'src/main/resources/js/iframe/plugin/*.js', included: false}
    ],


    // list of files to exclude
    exclude: [
    ],


    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', 'junit'],
    junitReporter: {
      outputFile: 'target/surefire-reports/karma-iframe-results.xml',
      suite: ''
    },


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,


    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: ['Chrome'],


    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 60000,


    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false
  });
};
