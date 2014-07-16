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
      {pattern: 'node_modules/karma-sinon/node_modules/sinon/lib/sinon/util/timers_ie.js', included: true},
      {pattern: 'node_modules/atlassian-connect-js/bower_components/jquery/jquery.js', included: true},
      {pattern: 'node_modules/atlassian-connect-js/bower_components/aui-dist/aui/js/aui.js', included: true},
      'http://aui-cdn.atlassian.com/aui-adg/5.4.3/js/aui-soy.js',
      {pattern: 'node_modules/atlassian-connect-js/bower_components/aui/src/js/atlassian.js', included: true},
      'src/main/resources/js/core/host-debug.js',
      'src/test/resources/test-main.js',
      {pattern: 'src/test/resources/**/*-test.js', included: false},
      {pattern: 'src/test/resources/fixtures/**', included: false},
      {pattern: 'target/qunit/dependencies/**/*.js', included: false},
      {pattern: 'src/main/resources/**/*.js', included: false}
    ],


    // list of files to exclude
    exclude: [
      'src/test/resources/js/iframe/plugin/*-test.js'
    ],

    //do not process my html files.
    preprocessors: {
      'src/test/resources/fixtures/!(*).html': ['html2js']
    },

    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['progress', 'junit'],
    junitReporter: {
      outputFile: 'target/surefire-reports/karma-results.xml',
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
    //browsers: ['Chrome', 'Safari', 'Firefox', 'Opera', 'IE11 - Win7', 'IE10 - Win7', 'IE9 - Win7'],
    browsers: ['PhantomJS'],


    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 60000,


    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: true
  });
};
