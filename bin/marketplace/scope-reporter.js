var downloader = require('./download-descriptors'),
    xml2js = require('xml2js'),
    _ = require('lodash'),
    util = require('util');

downloader.run({
    descriptorDownloadedCallback: function (addonKey, filename, type, body, opts) {
        var scopes = [];
        if (type === 'json') {
            scopes = JSON.parse(body).scopes;
        } else {
            console.log(addonKey, "XML descriptor not supported");
        }

        scopes = _.uniq(_.flatten(scopes));

        if (scopes.length) {
            console.log(addonKey + " : " + util.inspect(scopes, { colors: true }));
        }
    }
});

