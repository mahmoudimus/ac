(function (global) {
  var doc = global.document,
    RA = global.RA = {},
    rpc,
    dialogHandlers = {},
    isInited;

  // universal iterator utility
  function each(o, it) {
    var l, k;
    if (o) {
      l = o.length;
      if (l > 0) {
        k = 0;
        while (k < l) {
          if (it.call(o[k], k, o[k]) === false) break;
          k += 1;
        }
      }
      else {
        for (k in o) {
          if (o.hasOwnProperty(k)) {
            if (it.call(o[k], k, o[k]) === false) break;
          }
        }
      }
    }
  }

  // simple mixin util; if last arg is a fn, only
  function extend(dest) {
    var args = arguments,
        end = args.length,
        last = args[end - 1],
        when;
    if (typeof last === "function") {
      when = last;
      end -= 1;
    }
    var srcs = [].slice.call(args, 1, end);
    each(srcs, function (i, src) {
      each(src, function (k, v) {
        if (!when || when(k, v)) {
          dest[k] = v;
        }
      });
    });
    return dest;
  }

  // simple event binding
  function bind(el, e, fn) {
    var add = "addEventListener",
        attach = "attachEvent";
    if (el[add]) {
      el[add](e,fn,false);
    }
    else if (el[attach]) {
      el[attach]("on" + e, fn);
    }
  }

  // simple onload
  var isLoaded;
  function onload(fn) {
    if (isLoaded) {
      fn();
    }
    else {
      bind(global, "load", fn);
    }
  }
  onload(function () { isLoaded = true; });

//  // simple dom ready
//  function onReady(fn) {
//    if ((/in/).test(doc.readyState)) {
//      setTimeout(function () { onReady(fn); }, 9);
//    }
//    else {
//      fn();
//    }
//  }
//  var readyHandlers = [];
//  var isReady;
//  onReady(function () {
//    var i;
//    isReady = true;
//    for (i = 0; i < readyHandlers.length; i += 1) {
//      readyHandlers[i]();
//    }
//    readyHandlers = null; // release to gc
//  });

  // basic dom util
  function $(sel) {
    var els = [];
    if (sel) {
      if (typeof sel === "string") {
        var results = doc.querySelectorAll(sel);
        each(results, function (i, v) { els.push(v); });
      }
      else if (sel.nodeType === 1) {
        els.push(sel);
      }
// rest of dom ready support
//      else if (typeof sel === "function") {
//        if (isReady) {
//          sel();
//        }
//        else {
//          readyHandlers.push(sel);
//        }
//      }
    }
    extend(els, {
      each: function (it) { each(this, it); },
      append: function (spec) {
        this.each(function (i, to) {
          var el = doc.createElement(spec.tag);
          each(spec, function (k, v) {
            if (k === "$text") {
              el.appendChild(doc.createTextNode(v));
            }
            else if (k !== "tag") {
              el[k] = v;
            }
          });
          to.appendChild(el);
        });
      }
    });
    return els;
  }

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

  // attempts to make the RA doc conform to the host app, accepting the following options to RA.init:
  //
  // @options
  //   @value base    set to false to disable base tag injection
  //   @value css     set to false to disable css property injection
  //   @value xcss    blacklist regex of css property names to omit during css injection
  //   @value resize  set to false to disable automatic invocation of RA.resize
  function makeSeamless(options) {
    injectBase(options);
    injectCss(options, resize);
  }

  function injectBase(options) {
    if (options.base !== false) {
      // set the url base
      rpc.getLocation(function (loc) {
        $("head").append({tag: "base", href: loc, target: "_parent"});
      });
    }
  }

  function injectCss(options, next) {
    if (options.css !== false) {
      rpc.getStylesheet(function (rules) {
        var stylesheet = makeStylesheet(rules, options.xcss);
        $("head").append({tag: "style", type: "text/css", $text: stylesheet});
        next(options);
      });
    }
    else {
      next(options);
    }
  }

  function makeStylesheet(rules, xcss) {
    var filter, defaults = {
      "background-color": "transparent",
      "margin": "0",
      "padding": "0"
    };
    if (xcss) {
      filter = function (k, v) {
        return v != null && !xcss.test(k);
      }
    }
    var text = "";
    each(rules, function (i, rule) {
      text += rule.selector + " {";
      var props = rule.properties;
      props = extend({}, defaults, props, filter);
      each(props, function (k, v) {
        text += k + ":" + v + ";";
      });
      text += "}\n";
    });
    return text;
  }

  function resize(options) {
    if (options.resize !== false) {
      // set the initial iframe size on window load
      onload(function () { RA.resize(); });
    }
  }

  var api = {

    // inits the remote app on iframe content load
    init: function (options) {
      if (!isInited) {
        isInited = true;
        options = options || {};
        // create the rpc bridge
        var config = {};
        // init stubs for private methods
        var stubs = {getStylesheet: {}};
        // add stubs for each public api
        each(api, function (method) { stubs[method] = {}; });
        // create and init the rpc bridge
        rpc = new easyXDM.Rpc(config, {remote: stubs, local: internal});
        rpc.init();
        // integrate the iframe with its parent document
        makeSeamless(options);
      }
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
    // @param id    the message id
    // @param title   the message title
    // @param body    the message body
    showMessage: function (id, title, body) {
      rpc.showMessage(id, title, body);
    },

    // clears a message by id in the host application
    //
    // @param id    the message id
    clearMessage: function (id) {
      rpc.clearMessage(id);
    },

    // resize this iframe
    //
    // @param width   the desired width
    // @param height  the desired height
    resize: function (width, height) {
      var w = width == null ? "100%" : width;
      var h = height == null ? $("body")[0].scrollHeight : height;
      rpc.resize(w, h);
    },

    // execute an XMLHttpRequest in the context of the host application
    //
    // @param url     either the URI to request or an options object (as below) containing at least a 'url' property;
    //          this value should be relative to the context path of the host application
    // @param options   an options object containing one or more of the following properties:
    //          - url       the url to request from the host application, relative to the host's context path; required
    //          - type      the HTTP method name; defaults to 'GET'
    //          - data      the string entity body of the request; required if type is 'POST' or 'PUT'
    //          - contentType   the content-type string value of the entity body, above; required when data is supplied
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
