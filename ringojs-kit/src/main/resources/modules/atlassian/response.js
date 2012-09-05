var {Deferred} = require("atlassian/deferred");
var renderer = require("atlassian/renderer");
var fs = require("fs");

module.exports = function () {

  var deferred = Deferred();

  var data = {
    status: 200,
    headers: {"Content-Type": "text/html"},
    body: []
  };

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
      try {
        var body = renderer.render("app/views/" + view, locals);
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
