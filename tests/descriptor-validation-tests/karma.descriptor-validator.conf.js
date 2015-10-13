module.exports = function(config) {
  config.set({
    basePath: '',
    frameworks: ['requirejs', 'qunit', 'sinon'],
    files: [
        'target/descriptor-validation-results.json',
        'descriptor-test-main.js',
        'test-descriptor-validation.js'
    ],
    preprocessors: {
        'target/descriptor-validation-results.json': ['html2js']
    },
    reporters: ['progress', 'junit'],
    junitReporter: {
      outputFile: 'target/surefire-reports/karma-descriptor-results.xml',
      suite: ''
    },
    port: 9876,
    colors: true,
    logLevel: config.LOG_DEBUG,
    autoWatch: true,
    browsers: ['Chrome'],
    captureTimeout: 60000,
    singleRun: false
  });
};
