AP.define("request", ["_dollar", "_rpc"], function ($, rpc) {

  "use strict";

  var each = $.each,
      extend = $.extend;

  // internal maker that converts bridged xhr data into an xhr-like object
  function Xhr(data) {
    // copy the xhr data into a new xhr instance
    var xhr = extend({}, data);
    // store header data privately
    var headers = data.headers || {};
    // clear the headers map from the new instance
    delete xhr.headers;
    return extend(xhr, {
      // get header by name, case-insensitively
      getResponseHeader: function (key) {
        var value = null;
        if (key) {
          key = key.toLowerCase();
          each(headers, function (k, v) {
            if (k.toLowerCase() === key) {
              value = v;
              return false;
            }
          });
        }
        return value;
      },
      // get all headers as a formatted string
      getAllResponseHeaders: function () {
        var str = "";
        each(headers, function (k, v) {
          // prepend crlf if not the first line
          str += (str ? "\r\n" : "") + k + ": " + v;
        });
        return str;
      }
    });
  }

  /**
  * @name RequestProperties
  * @description An object containing the options of a {@link Request}
  * @class
  * @property {String}    url         the url to request from the host application, relative to the host's context path
  * @property {String}    type        the HTTP method name; defaults to 'GET'
  * @property {Boolean}   cache       if the request should be cached. Default is true.
  * @property {String}    data        the string entity body of the request; required if type is 'POST' or 'PUT'
  * @property {String}    contentType the content-type string value of the entity body, above; required when data is supplied
  * @property {Object}    headers     an object containing headers to set; supported headers are: Accept
  * @property {Function}  success     a callback function executed on a 200 success status code
  * @property {Function}  error       a callback function executed when a HTTP status error code is returned
  */


  var apis = rpc.extend(function (remote) {

    return {

      /**
      * @exports request
      */
      apis: {

        /**
        * execute an XMLHttpRequest in the context of the host application
        *
        * @param {String} url either the URI to request or an options object (as below) containing at least a 'url' property;<br />
        *                     this value should be relative to the context path of the host application.
        * @param {RequestProperties} options an RequestProperties object.
        * @example
        * // Display an alert box with a list of JIRA dashboards using the JIRA REST API.
        * AP.require('request', function(request){
        *   request({
        *     url: '/rest/api/2/dashboard',
        *     success: function(responseText){
        *       alert(responseText);
        *     }
        *   });
        * });
        */

        request: function (url, options) {
          var success, error;
          // unpacks bridged success args into local success args
          function done(args) {
            return success(args[0], args[1], Xhr(args[2]));
          }
          // unpacks bridged error args into local error args
          function fail(args) {
            return error(Xhr(args[0]), args[1], args[2]);
          }
          // normalize method arguments
          if (typeof url === "object") {
            options = url;
          }
          else if (!options) {
            options = {url: url};
          }
          else {
            options.url = url;
          }
          // no-op
          function nop() {}
          // extract done/fail handlers from options and clean up for serialization
          success = options.success || nop;
          delete options.success;
          error = options.error || nop;
          delete options.error;
          // execute the request
          remote.request(options, done, fail);
        }

      }

    };

  });

  return apis.request;

});
