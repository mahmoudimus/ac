(function (global) {
    var RA = global.RA = {};
    var rpc;
    var dialogHandlers = {};

    // universal iterator utility
    function each(o, it) {
        var l, k, v;
        if (o) {
            l = o.length;
            if (l > 0) {
                k = 0;
                while (k < l) {
                    v = o[k];
                    if (it.call(v, k, v) === false) break;
                    k += 1;
                }
            }
            else {
                for (k in o) {
                    if (o.hasOwnProperty(k)) {
                        v = o[k];
                        if (it.call(v, k, v) === false) break;
                    }
                }
            }
        }
    }

    // simple object mixin utility
    function extend(dest, src) {
        each(src, function (k, v) { dest[k] = v; });
        return dest;
    }

    // internal maker fn that converts bridged xhr data into an xhr-like object
    function Xhr(data) {
        // copy the xhr data into a new xhr instance
        var xhr = extend({}, data);
        // store header data privately
        var headers = data.headers || {};
        // clear the headers map from the new instance
        delete xhr.headers;
        // get header by name, case-insensitively
        xhr.getResponseHeader = function (key) {
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
        };
        // get all headers as a formatted string
        xhr.getAllResponseHeaders = function () {
            var str = "";
            each(headers, function (k, v) {
                // prepend crlf if not the first line
                str += (str ? "\r\n" : "") + k + ": " + v;
            });
            return str;
        };
        return xhr;
    }

    var api = {

        // inits the remote app on iframe content load
        init: function () {
            // create the rpc bridge
            rpc = new easyXDM.Rpc({}, {
                remote: (function () {
                    var stubs = {};
                    each(api, function (method) { stubs[method] = {}; });
                    return stubs;
                }()),
                local: internal
            });
            rpc.init();
            // set the initial iframe size
            RA.resize();
        },

        // get the location of the host page
        //
        // @param callback  function (location) {...}
        getLocation: function (callback) {
            rpc.getLocation(callback);
        },

        // get a user object containing the user's id and full name
        //
        // @param callback  function (user) {...}
        getUser: function (callback) {
            rpc.getUser(callback);
        },

        // shows a message with body and title by id in the host application
        //
        // @param id        the message id
        // @param title     the message title
        // @param body      the message body
        showMessage: function (id, title, body) {
            rpc.showMessage(id, title, body);
        },

        // clears a message by id in the host application
        //
        // @param id        the message id
        clearMessage: function (id) {
            rpc.clearMessage(id);
        },

        // resize this iframe
        //
        // @param width     the iframe width
        // @param height    the iframe height
        resize: function (width, height) {
            var w = width == null ? "100%" : width;
            var h = height == null ? (document.body.offsetHeight + 40) : height;
            rpc.resize(w, h);
        },

        // execute an XMLHttpRequest in the context of the host application
        //
        // @param url       either the URI to request or an options object (as below) containing at least a 'url' property;
        //                  this value should be relative to the context path of the host application
        // @param options   an options object containing one or more of the following properties:
        //                  - url           the url to request from the host application, relative to the host's context path; required
        //                  - type          the HTTP method name; defaults to 'GET'
        //                  - data          the string entity body of the request; required if type is 'POST' or 'PUT'
        //                  - contentType   the content-type string value of the entity body, above; required when data is supplied
        request: function (url, options) {
            var success, error;
            // unpacks bridged success args into local success args
            function done(args) {
                return success(args[0], args[1], Xhr(args[2]));
            }
            // unpacks bridged error args into local error args
            function fail(err) {
                var args = err.message;
                return error(Xhr(args[0]), args[1], args[2]);
            }
            // shared no-op
            function nop() {}
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
            // extract done/fail handlers from options and clean up for serialization
            success = options.success || nop;
            delete options.success;
            error = options.error || nop;
            delete options.error;
            // execute the request
            rpc.request(options, done, fail);
        },

        // dialog-related sub-api for use when the remote app is running as the content of a host dialog
        Dialog: {

            // register callbacks responding to messages from the host dialog, such as "submit" or "cancel"
            onDialogMessage: function(messageName, callback) {
                dialogHandlers[messageName] = callback;
            }

        }

    };

    // internal bridge callbacks for handling rpc invocations from the host application
    var internal = {

        // forwards dialog event messages from the host application to locally registered handlers
        dialogMessage: function(message) {
            // if no handler, default to allowing the operation to proceed
            return dialogHandlers[message]? dialogHandlers[message]() : true;
        }

    };

    // reveal the api on the RA global
    each(api, function (k, v) { RA[k] = v; });

})(this);
