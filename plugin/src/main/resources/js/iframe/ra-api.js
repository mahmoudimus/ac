(function (global) {
  var doc = global.document,
      appDoc = doc,
      RA = global.RA = {},
      rpc,
      dialogHandlers = {},
      isInited;

  // universal iterator utility
  function each(o, it) {
    var l, k;
    if (o) {
      l = o.length;
      if (l != null) {
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

  // simple mixin util
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

  // basic dom util
  function $(sel, context) {
    context = context || appDoc;
    var els = [];
    if (sel) {
      if (typeof sel === "string") {
        var results = context.querySelectorAll(sel);
        each(results, function (i, v) { els.push(v); });
      }
      else if (sel.nodeType === 1) {
        els.push(sel);
      }
    }
    extend(els, {
      each: function (it) { each(this, it); },
      append: function (spec) {
        this.each(function (i, to) {
          var el = context.createElement(spec.tag);
          each(spec, function (k, v) {
            if (k === "$text") {
              if(el.styleSheet) { // style tags in ie
                el.styleSheet.cssText = v;
              }
              else {
                el.appendChild(context.createTextNode(v));
              }
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

  // a simplified version of underscore's debounce
  function debounce(fn, wait) {
    var timeout;
    return function() {
      var ctx = this,
        args = [].slice.call(arguments);
      function later() {
        timeout = null;
        fn.apply(ctx, args);
      }
      if (timeout) {
        clearTimeout(timeout);
      }
      timeout = setTimeout(later, wait || 50);
    };
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

  function injectBase(options) {
    if (options.base !== false) {
      // set the url base
      RA.getLocation(function (loc) {
        $("head").append({tag: "base", href: loc, target: "_parent"});
      });
    }
  }

  function size(width, height) {
    var w = width == null ? "100%" : width,
      max = Math.max,
      body = appDoc.body,
      del = appDoc.documentElement,
      scroll = "scrollHeight",
      offset = "offsetHeight",
      client = "clientHeight",
      dh = max(del[client], body[scroll], del[scroll], body[offset], del[offset]),
      h = height == null ? dh : height;
    return {w: w, h: h};
  }

  function initNormal(options) {
    if (options.document) {
      appDoc = options.document;
    }
    var config = {};
    // init stubs for private bridge functions
    var stubs = {/* private repc stubs here */};
    // add stubs for each public api
    each(api, function (method) { stubs[method] = {}; });
    rpc = new easyXDM.Rpc(config, {remote: stubs, local: internal});
    rpc.init();
    // integrate the iframe with the host document
    if (options.base !== false) {
      // inject an appropriate base tag
      injectBase(options);
    }
    if (options.resize !== false) {
      // resize the parent iframe for the size of this document on load
      bind(options.window || global, "load", function () { RA.resize(); });
    }
  }

  function initBridge() {
    var sup = RA.resize;
    // on resize, resize frame in this doc then prop to parent
    RA.resize = debounce(function (w, h) {
      $("iframe", doc).each(function (i, iframe) {
        var dim = size(w, h);
        w = dim.w;
        h = dim.h;
        iframe.width = typeof w === "number" ? w + "px" : w;
        iframe.height = typeof h === "number" ? h + "px" : h;
      });
      sup.call(RA, w, h);
    }, 50);
  }

  function initBridged(options) {
    RA = global.RA = parent.RA;
    options = extend({}, options, {
      window: global,
      document: doc,
      base: false
    });
    RA.init(options);
  }

  var api = {

    // inits the remote plugin on iframe content load
    init: function (options) {
      options = options || {};
      var isBridged;
      try { isBridged = !!parent.RA; } catch (ignore) { }
      if (isBridged) {
        initBridged(options);
      }
      else if (!isInited) {
        if (options === "bridge") {
          initBridge();
        }
        else {
          initNormal(options);
          isInited = true;
        }
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
      var dim = size(width, height);
      rpc.resize(dim.w, dim.h);
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

    // dialog-related sub-api for use when the remote plugin is running as the content of a host dialog
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
