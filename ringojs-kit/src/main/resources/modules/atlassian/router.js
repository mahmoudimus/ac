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
          var params = [];
          pattern = pattern
            .replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&")
            .replace(/\/\\\*$/g, "(/.*|$)")
            .replace(/\/:([\w\d\-_]+)/g, function ($0, $1) {
              params.push($1);
              return "/([^/]+)";
            });
          pattern = new RegExp("^" + pattern + "$");
          routes[method].push({
            pattern: pattern,
            params: params,
            handler: handler
          });
        };
      });
      return app;
    }()),

    router: function (req) {
      var pathSplit = req.scriptName.indexOf("/", 1);
      var path = req.pathInfo = req.scriptName.slice(pathSplit);

      // fix-up/amend request
      req.scriptName = req.scriptName.slice(0, pathSplit);
      contextKeys.forEach(function (k) {
        req[k] = context[k]();
      });

      var handler;
      req.params = {};
      routes[req.method.toLowerCase()].every(function (route) {
        var matches = route.pattern.exec(path);
        if (matches) {
          handler = route.handler;
          route.params.forEach(function (name, i) {
            var value = matches[i + 1];
            if (!req[name]) req[name] = value;
            req.params[name] = value;
          });
          return false;
        }
        return true;
      });

      var res = Response();

      if (!handler) {
        handler = function () {
          res.sendNotFound();
        }
      }

      handler(req, res);
      return res._toJsgiResponse();
    }

  };

};
