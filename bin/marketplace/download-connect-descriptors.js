#!/usr/bin/env node

var request = require('request'),
    _ = require('lodash'),
    fs = require('fs'),
    util = require('util');

var downloadDestination = "descriptors/",
    baseUrl = "https://marketplace.atlassian.com";
    uri = baseUrl + "/rest/1.0/plugins?hosting=ondemand&addOnType=three&limit=50";

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

                var descriptorUrl = _.find(version.links, { 'rel': 'descriptor' }).href;

                downloadDescriptor(key, descriptorUrl);
            });

            if (!!nextRequestUri) {
                getAddonPage(baseUrl + nextRequestUri);
            }
        }
    });
};

var downloadDescriptor = function (addonKey, descriptorUrl) {
    request({
        uri: descriptorUrl,
        method: "GET"
    }, function (error, response, body) {
        if (error) {
            console.log("Unable to download descriptor for add-on", addonKey);
        } else { 

            var fileName = "descriptors/" + addonKey + '-descriptor';
            fs.writeFile(fileName, body, function (err) {
                if (err) {
                    console.log("Unable to write descriptor for add-on " + addonKey + " to disk", err);
                } else {
                    console.log("Downloaded descriptor", fileName);
                }
            });
        }
    });
}

function go() {
    if (!fs.existsSync(downloadDestination)) {
        fs.mkdirSync(downloadDestination);
    }
    getAddonPage(uri);
};

go();