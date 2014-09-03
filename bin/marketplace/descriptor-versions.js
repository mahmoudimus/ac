#!/usr/bin/env node

var request = require("request"),
    events = require("events"),
    extend = require("node.extend"),
    colors = require("colors"),
    nomnom = require("nomnom");

var runningAsScript = require.main === module;

var defaultOpts = {
    baseUrl: "https://marketplace.atlassian.com"
};

function DescriptorVersions() {

    events.EventEmitter.call(this);

    this.load = function (addonKey, opts) {

        var self = this;

        request({
            uri: opts.baseUrl + "/rest/1.0/plugins/" + addonKey + "?hosting=ondemand",
            method: "GET",
            auth: opts.auth,
            json: true
        }, function (error, response, body) {
            if (error || response.statusCode !== 200 ||
                (response.statusCode === 200 && (!body || !body.versions || !Array.isArray(body.versions.versions)))) {
                self.emit("error", error, response, body)
            } else {
                body.versions.versions.forEach(function (version) {
                    self.emit("version", version);
                });
            }
        });
    }
}

DescriptorVersions.prototype = Object.create(events.EventEmitter.prototype);
DescriptorVersions.prototype.constructor = DescriptorVersions;

exports = new DescriptorVersions();


if (runningAsScript) {

    function descriptorTypeColored(type) {
        return type === "json" ? type["green"] : type["red"];
    }

    function statusColored(status) {
        return status.toLowerCase() === "public" ? status["green"] : status["red"];
    }

    function stableColored(stable) {
        return stable === true ? "stable".green : "unstable".red;
    }

    var cliOpts = nomnom
        .option("addon", {
            abbr: "a",
            help: "Add-on key",
            required: true
        })
        .option("debug", {
            abbr: "d",
            flag: true,
            help: "Print debugging info"
        })
        .option("verbose", {
            abbr: "v",
            flag: true,
            help: "Print additional Marketplace info"
        })
        .option("user", {
            abbr: "u",
            help: "Marketplace username"
        })
        .option("pass", {
            abbr: "p",
            help: "Marketplace password"
        }).parse();

    var opts = extend({}, defaultOpts);

    if (cliOpts.user && cliOpts.pass) {
        opts.auth = {
            username: cliOpts.user,
            password: cliOpts.pass
        }
    }

    opts.debug = cliOpts.debug;

    if (opts.debug) {
        console.dir(opts);
        console.log("Add-On:", cliOpts.addon);
    }

    var versions = new DescriptorVersions();

    versions.on("error", function (err, response, body) {
        if (err) {
            console.error(err);
        } else if (response.statusCode === 404) {
            console.error("No descriptor found for add-on '" + cliOpts.addon + "'")
        } else if (response.statusCode !== 200) {
            console.error("Error response from Marketplace: ", response.statusCode);
        } else if (!body) {
            console.error("Empty response from Marketplace");
        } else {
            console.error("Unexpected body format:", body);
        }
    });

    versions.on("version", function (version) {
        var versionSummary = [
            version.version,
            version.releaseDate,
            statusColored(version.status),
            descriptorTypeColored(version.deployment.descriptorType),
            stableColored(version.stable)
        ];

        if (cliOpts.verbose) {
            versionSummary.push(
                version.marketplaceType.type,
                version.releasedBy,
                version.summary);
        }

        console.log(versionSummary.join("|".grey));
    });

    versions.load(cliOpts.addon, opts)
}

