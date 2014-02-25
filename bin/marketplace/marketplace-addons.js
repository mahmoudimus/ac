
var request = require('request'),
    _ = require('lodash'),
    util = require('util');

var baseUrl = "https://marketplace.atlassian.com";
    uri = baseUrl + "/rest/1.0/plugins?hosting=ondemand&addOnType=three&limit=20";

var getAddonPage = function (uri) {
    return request({
        uri: uri,
        method: "GET",
        json: true
    }, function (error, response, body) {
        if (error) {
            console.log("Unable to retrieve marketplace add-ons", response, error);
            return null;
        } else {
            var links = body.links,
                addons = body.plugins;

            var nextRequestLink = _.find(links, {
                    'rel': 'next',
                    'type': 'application/json'
                }),
                nextRequestUri = nextRequestLink ? nextRequestLink.href : null;

            _.forEach(addons, function (addon) {
                if (addon.isOldVersion) {
                    return;
                }

                var name = addon.name,
                    key = addon.pluginKey,
                    version = addon.version;

                if (version.pluginSystemVersion !== 'Three') {
                    return;
                }

                var descriptor = _.find(version.links, { 'rel': 'descriptor' }).href;

                console.log(key, '\t', descriptor);
            });

            if (!!nextRequestUri) {
                getAddonPage(baseUrl + nextRequestUri);
            }
        }
    });
};

getAddonPage(uri);