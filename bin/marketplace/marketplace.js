#!/usr/bin/env node

var request = require('request'),
        _ = require('lodash'),
        async = require('async'),
        extend = require('node.extend'),
        nomnom = require('nomnom'),
        colors = require('colors'),
        util = require('util');

var selfExecute = require.main === module;

var downloadQueue = async.queue(function (task, callback) {
  var args = task.args;
  args.push(callback);
  task.executor.apply(task.self || this, args);
}, 3);

downloadQueue.drain = function () {
};

var includePrivateAddons = function(opts) {
  return _.contains(opts.status, 'private');
};

var includeFreeAddons = function(opts) {
  return _.contains(opts.cost, 'free');
};

var includePaidAddons = function(opts) {
  return _.contains(opts.cost, 'paid');
};

var executeRequest = function(opts, uri, failure, success) {
  return request({
    uri: opts.baseUrl + uri,
    method: "GET",
    auth: opts.auth,
    json: true
  }, function(error, response, body) {
    if (error || (response.statusCode < 200 || response.statusCode > 299)) {
      console.log("ERROR".red + ": Unable to retrieve marketplace add-ons", response.statusCode, error || body);
      failure(error);
    } else {
      success(response, body);
    }
  });
}

var getAddon = function(opts, uri, callback) {
  if (opts.debug) {
    console.log("downloading: ".grey + opts.baseUrl + uri);
  }

  return executeRequest(opts, uri, callback, function (response, body) {
    console.log(body);
  });
}

var getAddonPage = function(opts, uri, callback) {
  if (opts.debug) {
    console.log("downloading: ".grey + opts.baseUrl + uri);
  }

  return executeRequest(opts, uri, callback, function (response, body) {
    var links = body.links,
            addons = body.plugins;

    var nextRequestLink = _.find(links, {
              'rel': 'next',
              'type': 'application/json'
            }),
            nextRequestUri = nextRequestLink ? nextRequestLink.href : null;

    _.forEach(addons, function(addon) {
      var version = addon.version,
              deployment = version.deployment,
              releaseDate = version.releaseDate || "(unknown date)";
      if (!deployment || !deployment.remote) {
        console.log("No deployment info for " + addon.pluginKey.red);
        return false;
      }

      var status = addon.approval.status.toLowerCase(),
              statusColor = status === 'public' ? 'blue' : 'red',
              url = opts.baseUrl + _.find(addon.links, {
                        'rel': 'self'
                      }).href;

      if (opts.status && !_.contains(opts.status, status)) {
        return;
      }

      if (!opts.quiet) {
        try {
          if (opts.debug || selfExecute) {
            console.log(addon.pluginKey, "(" + status[statusColor] + ")", opts.debug ? (releaseDate + " " + url.grey) : "");
          }
        } catch (e) {
          if (opts.debug) {
            console.log(("" + e).red);
            console.log(JSON.stringify(addon));
          }
          callback(e);
        }
      }

      if (opts.marketplaceAddonCallback) {
        opts.marketplaceAddonCallback(addon, opts);
      }
    });

    if (nextRequestUri) {
      var nextOpts = extend({}, opts);
      downloadQueue.push({
        self: this,
        executor: getAddonPage,
        args: [nextOpts, nextRequestUri]
      }, function () { });
    }

    callback();
  });
};

var getCliOpts = function(callback) {
  var nn = nomnom
          .option('debug', {
            abbr: 'd',
            flag: true,
            help: 'Print debugging info'
          })
          .option('user', {
            abbr: 'u',
            help: 'Marketplace username'
          })
          .option('pass', {
            abbr: 'p',
            help: 'Marketplace password'
          })
          .option('addon', {
            abbr: 'a',
            help: 'Operate only on specified add-ons'
          })
          .option('quiet', {
            abbr: 'q',
            flag: true,
            help: 'Don\'t debug spam'
          })
          .option('status', {
            abbr: 's',
            list: true,
            choices: ['private', 'public', 'rejected'],
            help: 'Filter add-ons by status. Valid options: [private, public, rejected]'
          })
          .option('cost', {
            abbr: 'c',
            help: 'Add-on payment model. Valid options: [free, paid] (includes both if unspecified)',
            choices: ['free', 'paid']
          });

  if (callback) {
    nn = callback(nn);
  }
  return nn;
}

var defaultOpts = {
  baseUrl: "https://marketplace.atlassian.com",
  uri: "/rest/1.0/plugins"
}

exports.run = function(runOpts) {
  var nomnom = getCliOpts(runOpts ? runOpts.cliOptsCallback : false);

  var opts = extend({}, defaultOpts);
  opts = extend(opts, runOpts);
  opts = extend(opts, nomnom.parse());

  var uri = opts.uri;
  delete opts["uri"];

  if (!opts.addon) {
    uri = uri + "?hosting=ondemand&addOnType=three";

    if (includePrivateAddons(opts)) {
      uri = uri + "&includePrivate=true";
    }

    if (includeFreeAddons(opts)) {
      uri = uri + "&cost=free";
    }

    if (includePaidAddons(opts)) {
      uri = uri + "&cost=marketplace";
    }
  } else {
    uri = uri + "/" + opts.addon;
  }

  if (opts.user && opts.pass) {
    opts.auth = {
      username: opts.user,
      password: opts.pass
    }
  }

  if (opts.before) {
    opts.before(opts);
  }

  downloadQueue.push({
    self: this,
    executor: opts.addon ? getAddon : getAddonPage,
    args: [opts, uri]
  }, function () { });
};

exports.requestQueue = function () {
  return downloadQueue;
}

if (selfExecute) {
  exports.run();
}