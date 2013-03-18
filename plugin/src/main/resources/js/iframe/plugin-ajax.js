var request = AP.require("request");

AP.define("_ajax", ["_util", "env"], function (util, env) {

  "use strict";

  var window = this,
      JSON = window.JSON;

  return {

    // only supports GETs for now
    ajax: function (options) {
      var xhr;
      xhr = (window.ActiveXObject && new ActiveXObject("Microsoft.XMLHTTP"));
      if (!xhr) xhr = window.XMLHttpRequest && new XMLHttpRequest();
      xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
          var status = xhr.status,
            response = xhr.responseText,
            contentType = xhr.getResponseHeader("Content-Type"),
            nop = function () {},
            success = options.success || nop,
            error = options.error || nop,
            body;
          if (status >= 200 && status <= 300) {
            if (contentType && contentType.indexOf("application/json") === 0) {
              try {
                body = JSON.parse(response);
                try {
                  success(body, xhr.statusText);
                }
                catch (ex) {
                  util.handleError(ex);
                }
              }
              catch (ex) {
                error(xhr, "parseerror", ex);
              }
            }
            else {
              success(response, xhr.statusText);
            }
          }
          else {
            error(xhr, xhr.statusText || "error");
          }
        }
      };
      xhr.open("GET", options.url, true);
      var headers = options.headers || {};
      headers["AP-Auth-State"] = env.meta("auth-state");
      util.each(options.headers, function (k, v) { xhr.setRequestHeader(k, v); });
      xhr.send(null);
      return xhr;
    }

  };

});
