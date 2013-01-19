(function (global) {
  var doc = global.document,
      appDoc = doc,
      AP = global.AP = global.RA = {}, // consider RA deprecated
      rpc,
      isDialog,
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
      each: function (it) {
        each(this, it);
        return this;
      },
      attr: function (k) {
        var v;
        this.each(function (i, el) {
          v = el[k] || (el.getAttribute && el.getAttribute(k));
          return !v;
        });
        return v;
      },
      removeClass: function (className) {
        return this.each(function (i, el) {
          if (el.className) {
            el.className = el.className.replace(new RegExp("(^|\\s)" + className + "(\\s|$)"), " ");
          }
        });
      },
      html: function (html) {
        return this.each(function (i, el) {
          el.innerHTML = html;
        });
      },
      append: function (spec) {
        return this.each(function (i, to) {
          var el = context.createElement(spec.tag);
          each(spec, function (k, v) {
            if (k === "$text") {
              if (el.styleSheet) { // style tags in ie
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

  function fetch(options) {
    var xhr =
      (global.ActiveXObject && new ActiveXObject("Microsoft.XMLHTTP")) ||
      (global.XMLHttpRequest && new XMLHttpRequest());
    xhr.onreadystatechange = function () {
      if (xhr.readyState === 4) {
        var status = xhr.status;
        var response = xhr.responseText;
        var contentType = xhr.getResponseHeader("Content-Type");
        if (status >= 200 && status <= 300) {
          if (contentType && contentType.indexOf("application/json") === 0) {
            try {
              if (options.success) options.success(JSON.parse(response), xhr.statusText);
            }
            catch (ex) {
              if (options.error) options.error(xhr, "parseerror", ex);
            }
          }
          else {
            if (options.success) options.success(response, xhr.statusText);
          }
        }
        else {
          if (options.error) options.error(xhr, xhr.statusText || "error");
        }
      }
    };
    xhr.open("GET", options.url, true);
    each(options.headers, function (k, v) { xhr.setRequestHeader(k, v); });
    xhr.send(null);
    return xhr;
  }

  // a simplified version of underscore's debounce
  function debounce(fn, wait) {
    var timeout;
    return function () {
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

  function handleError(err) {
    if (global.console) {
      console.error(err);
    }
    else {
      throw err;
    }
  }

  AP.Meta = {
    get: function (name) {
      return $("meta[name='ap-" + name + "']").attr("content");
    }
  };

  AP.BigPipe = function () {
    var started;
    var closed;
    var channels = {};
    var subscribers = {};
    var buffers = {};
    function poll(url) {
      fetch({
        url: url,
        headers: {"Accept": "application/json"},
        success: function (response) {
          deliver(response, url);
        },
        error: function (xhr, status, ex) {
          handleError(ex || (xhr && xhr.responseText) || status);
        }
      });
    }
    function deliver(response, url) {
      if (response) {
        var items = response.items;
        if (items.length > 0) {
          each(items, function (i, item) {
            publish(item);
          });
          if (response.pending.length > 0) {
            each(channels, function (channelId, open) {
              if (open && response.pending.indexOf(channelId) < 0) {
                close(channelId);
              }
            });
            poll(url);
          }
          else {
            close();
          }
        }
        else if (!closed) {
          close();
        }
      }
    }
    function publish(event) {
      var channelId = event.channelId;
      channels[channelId] = true;
      if (subscribers[channelId]) subscribers[channelId](event);
      else (buffers[channelId] = buffers[channelId] || []).push(event);
    }
    function close(channelId) {
      if (channelId) {
        publish({channelId: channelId, complete: true});
        delete subscribers[channelId];
        channels[channelId] = false;
      }
      else if (!closed) {
        each(subscribers, close);
        closed = true;
      }
    }
    var self = {
      start: function (options) {
        options = options || {};
        var requestId = options.requestId;
        var baseUrl = options.localBaseUrl;
        if (!started && baseUrl && requestId) {
          poll(baseUrl + "/bigpipe/request/" + requestId);
          started = true;
        }
      },
      subscribe: function (channelId, subscriber) {
        if (subscribers[channelId]) {
          throw new Error("Channel '" + channelId + "' already has a subscriber");
        }
        if (subscriber) {
          subscribers[channelId] = subscriber;
          each(buffers[channelId], function (i, result) {
            publish(result);
          });
          delete buffers[channelId];
          if (closed) close(channelId);
        }
      }
    };
    self.subscribe("html", function (event) {
      if (!event.complete) {
        $("#" + event.contentId).removeClass("bp-loading").html(event.content);
        AP.resize();
      }
    });
    return self;
  }();

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
    // set the url base
    AP.getLocation(function (loc) {
      $("head").append({tag: "base", href: loc, target: "_parent"});
    });
  }

  function injectMargin(options) {
    // set a context-sensitive margin value
    var margin = isDialog ? "10px 10px 0 10px" : "0";
    // @todo stylesheet injection here is rather heavy handed -- switch to setting body style
    $("head").append({tag: "style", type: "text/css", $text: "body {margin: " + margin + " !important;}"});
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
    isDialog = global.location.toString().indexOf("dialog=1") > 0;
    var config = {};
    // init stubs for private bridge functions
    var stubs = {
      setDialogButtonEnabled: {},
      isDialogButtonEnabled: {}
    };
    // add stubs for each public api
    each(api, function (method) { stubs[method] = {}; });
    rpc = new easyXDM.Rpc(config, {remote: stubs, local: internal});
    rpc.init();
    // integrate the iframe with the host document
    if (options.margin !== false) {
      // inject an appropriate margin value
      injectMargin(options);
    }
    if (options.base !== false) {
      // inject an appropriate base tag
      injectBase(options);
    }
    if (options.resize !== false) {
      // resize the parent iframe for the size of this document on load
      bind(options.window || global, "load", function () { AP.resize(); });
    }
    if (isDialog) {
      // expose the dialog sub-api if appropriate
      AP.Dialog = makeDialog();
    }
  }

  function initBridge() {
    var sup = AP.resize;
    // on resize, resize frame in this doc then prop to parent
    AP.resize = debounce(function (w, h) {
      $("iframe", doc).each(function (i, iframe) {
        var dim = size(w, h);
        w = dim.w;
        h = dim.h;
        iframe.width = typeof w === "number" ? w + "px" : w;
        iframe.height = typeof h === "number" ? h + "px" : h;
      });
      sup.call(AP, w, h);
    }, 50);
  }

  function initBridged(options) {
    AP = global.RA = parent.RA;
    options = extend({}, options, {
      window: global,
      document: doc,
      base: false
    });
    AP.init(options);
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
    }

  };

  // internal bridge callbacks for handling rpc invocations from the host application
  var internal = {

    // forwards dialog event messages from the host application to locally registered handlers
    dialogMessage: function (name) {
      var result = true;
      try {
        if (isDialog) {
          result = AP.Dialog.getButton(name).trigger();
        }
        else {
          handleError("Received unexpected dialog button event from host:", name);
        }
      }
      catch (e) {
        handleError(e);
      }
      return result;
    }

  };

  // dialog-related sub-api for use when the remote plugin is running as the content of a host dialog
  function makeDialog() {
    var listeners = {};
    return {

      // register callbacks responding to messages from the host dialog, such as "submit" or "cancel"
      //
      // @deprecated
      onDialogMessage: function (message, listener) {
        this.getButton(message).bind(listener);
      },

      // gets a button proxy for a button in the host's dialog button panel by name
      getButton: function (name) {
        return {
          name: name,
          enable: function () {
            rpc.setDialogButtonEnabled(name, true);
          },
          disable: function () {
            rpc.setDialogButtonEnabled(name, false);
          },
          toggle: function () {
            var self = this;
            self.isEnabled(function (enabled) {
              self[enabled ? "disable" : "enable"](name);
            });
          },
          isEnabled: function (callback) {
            rpc.isDialogButtonEnabled(name, callback);
          },
          bind: function (listener) {
            var list = listeners[name];
            if (!list) {
              list = listeners[name] = [];
            }
            list.push(listener);
          },
          trigger: function () {
            var self = this,
                cont = true,
                result = true,
                list = listeners[name];
            each(list, function (i, listener) {
              result = listener.call(self, {
                button: self,
                stopPropagation: function () { cont = false; }
              });
              return cont;
            });
            return !!result;
          }
        }
      }

    };
  }

  // reveal the api on the RA global
  extend(AP, api);

})(this);
