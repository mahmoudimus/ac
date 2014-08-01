#!/usr/bin/env node

var downloader = require('./download-descriptors'),
    _ = require('lodash'),
    util = require('util'),
    c = require('colors');

var macroIterator = function (macro) {
    if (macro.renderModes) {

    }
}

downloader.run({
    descriptorDownloadedCallback: function (result, body, opts) {
        if (result.type !== 'json') {
            console.log(result.type + " not supported");
            return;
        }
        var modules = JSON.parse(body).modules;
        if (modules) {
            var dynamicMacros = modules["dynamicContentMacros"];
            var staticMacros = modules["staticContentMacros"];

            var macros = [];
            macros.concat(dynamicMacros);
            macros.concat(staticMacros);
            
            macros = _.filter(macros, function (macro) {
                return !!macro.renderModes;
            });

            console.log(macros);
        }
        
    }
});

