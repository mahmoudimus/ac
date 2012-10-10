var {Deferred} = require("atlassian/deferred");
var renderer = require("atlassian/renderer");
var context = require("atlassian/context");
var fs = require("fs");

module.exports = function (appDir) {

  var deferred = Deferred();

  var data = {
    status: 200,
    headers: {"Content-Type": "text/html"},
    body: []
  };

  appDir = appDir || "./";
  if (appDir.charAt(appDir.length - 1) !== "/") appDir += "/";

  return {

    // response.writeHead(statusCode, headers)
    // response.writeHead(headers)
    writeHead: function (statusCode, headers) {
      if (typeof statusCode === "object") {
        headers = statusCode;
      }
      else {
        data.status = statusCode;
      }
      if (headers) {
        Object.keys(headers).forEach(function (k) {
          data.headers[k] = headers[k];
        });
      }
    },

    // response.write(chunk)
    write: function (chunk) {
      data.body.push(chunk);
    },

    // response.end()
    // response.end(chunk)
    end: function (chunk) {
      if (chunk != null) this.write(chunk);
      deferred.resolve();
    },

    // response.sendNotFound()
    // response.sendNotFound(reason)
    sendNotFound: function (reason) {
      reason = reason || "Not Found";
      this.writeHead(404);
      this.end(reason);
    },

    // response.send(body)
    // response.send(body, headers)
    // response.send(body, headers, statusCode)
    send: function (body, headers, statusCode) {
      this.writeHead(statusCode || 200, headers);
      this.end(body);
    },

    // response.render(view, locals)
    // response.render(view, locals, headers, statusCode)
    render: function (view, locals, headers, statusCode) {
      var allLocals = context.snapshot();
      Object.keys(locals).forEach(function (k) {
        allLocals[k] = locals[k];
      });
      try {
        var body = renderer.render(appDir + "views/" + view, allLocals);
        this.send(body, headers, statusCode);
      }
      catch (ex) {
        var je = ex.javaException;
        if (je && je.cause instanceof java.io.FileNotFoundException) {
          this.sendNotFound(je.cause.getMessage());
        }
        else {
          throw ex;
        }
      }
    },

    _toJsgiResponse: function () {
      deferred.wait();
      return data;
    }

  };

};
