var Response = require("atlassian/response");
var context = require("atlassian/context");
var methods = ["get", "post", "put", "del"];
var contextKeys = ["clientKey", "userId", "hostBaseUrl"];

// @todo is a new js context created on each request?
//       if not, this is inefficient as it recreates the whole routing table every time
//       if needed, try caching tables by app key?
// @todo improve request api before invoking route handler(s)
//       - expressjs-like path param format and parsing
//       - query string parsing

module.exports = function () {

  var routes = {};
  methods.forEach(function (method) { routes[method] = []; });

  return {

    app: (function () {
      var app = {
        all: function (route, handler) {
          methods.forEach(function (method) { app[method](route, handler); });
        }
      };
      methods.forEach(function (method) {
        app[method] = function (pattern, handler) {
          routes[method].push({
            // @todo regexp escaping, capturing group and glob conversions
            pattern: new RegExp("^" + pattern + "$"),
            handler: handler
          });
        };
      });
      return app;
    }()),

    router: function (req) {
      var handler;
      var path = req.scriptName.slice(req.scriptName.indexOf("/", 1));
      // @todo build defaultView from underscore-joined, non-param path elements
      var pathMatches = /^\/([\w\d\-_]+)/.exec(path);
      var defaultView = pathMatches ? pathMatches[1] : null;
      var res = Response(defaultView);

      routes[req.method.toLowerCase()].every(function (route) {
        // @todo extract capturing groups as req path params
        if (route.pattern.test(path)) {
          handler = route.handler;
          return false;
        }
        return true;
      });

      if (!handler) {
        handler = function () {
          res.sendNotFound();
        }
      }

      contextKeys.forEach(function (k) {
        req[k] = context[k]();
      });

      handler(req, res);
      return res._toJsgiResponse();
    }

  };

};
