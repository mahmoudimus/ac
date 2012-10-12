importPackage(com.atlassian.httpclient.api);
importPackage(com.google.common.util.concurrent);
importPackage(java.io);

var {Deferred} = require("./deferred");
var client = appContext.getBean("hostHttpClient");
var responseProps = "statusCode statusText contentType contentCharset entity".split(" ");

var api = module.exports = {

  // var promise = get(uri);
  // var promise = get({uri, headers});
  get: op("GET"),

  // var promise = post(uri, contentType, entity);
  // var promise = post({uri, headers, contentType, entity});
  post: op("POST", true),

  // var promise = put(uri, contentType, entity);
  // var promise = put({uri, headers, contentType, entity});
  put: op("PUT", true),

  // var promise = del(uri);
  // var promise = del({uri, headers});
  del: op("DELETE"),

  // var promise = request({method, uri, headers, contentType, entity});
  request: function (options) {
    var deferred = Deferred();
    try {
      // build the java request object from incoming args
      var request = client.newRequest();
      var method = options.method;
      Object.keys(options).forEach(function (k) {
        var headers;
        if (k === "headers") {
          headers = options[k];
          Object.keys(headers).forEach(function (name) {
            request.setHeader(name, headers[name]);
          });
        }
        else if (k === "uri") {
          request[k] = java.net.URI.create(options[k]);
        }
        else if (k !== "method") {
          // @todo entity marshalling
          request[k] = options[k];
        }
      });

      // execute the request
      request[method.toLowerCase()]().then({
        onSuccess: function (response) {
          // build a js response object from the java response
          var json = {};
          responseProps.forEach(function (k) {
            json[k] = response[k];
          });
          var headers = response.headers;
          json.headers = {};
          var keys = headers.keySet().iterator();
          while (keys.hasNext()) {
            var k = keys.next();
            json.headers[k] = headers.get(k);
          }
          // @todo entity marshalling
          deferred.resolve(json);
        },
        onFailure: function (ex) {
          // propagate the exception
          deferred.reject(ex, exstr(ex));
        }
      });
    }
    catch (ex) {
      // preflight exception
      deferred.reject(ex, exstr(ex));
    }
    return deferred.promise();
  }

};

function op(method, expectsEntity) {
  return function (uri, contentType, entity) {
    var options;
    if (typeof uri === "string") {
      options = {uri: uri};
      if (expectsEntity) {
        options.contentType = contentType;
        options.entity = entity;
      }
    }
    else {
      options = uri || {};
    }
    options.method = method;
    return api.request(options);
  };
}

function exstr(ex) {
  var str;
  ex = ex.javaException ? ex.javaException : ex;
  if (ex instanceof java.lang.Throwable) {
    var sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    str = sw.toString();
  }
  else {
    str = ex.stack || ex.toString();
  }
  return str;
}
