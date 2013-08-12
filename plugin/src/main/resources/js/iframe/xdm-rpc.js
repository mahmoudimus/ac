(window.AP || window._AP).define("_xdm-rpc", ["_dollar"], function ($) {

  "use strict";

  // Capture some common values and symbol aliases
  var loc = window.location.toString(),
      localOrigin = getBaseUrl(loc),
      count = 0,
      $window = $(window),
      isFn = $.isFunction;

  /**
   * Sets up cross-iframe remote procedure calls.
   * If this is called from a parent window, iframe is created and an RPC interface for communicating with it is set up.
   * If this is called from within the iframe, an RPC interface for communicating with the parent is set up.
   *
   * Calling a remote function is done with the signature:
   *     fn(data..., doneCallback, failCallback)
   * doneCallback is called after the remote function executed successfully.
   * failCallback is called after the remote function throws an exception.
   * doneCallback and failCallback are optional.
   *
   * @param {Object} config Configuration parameters
   * @param {String} config.remote src of remote iframe
   * @param {String} config.container id of element to which the generated iframe is appended
   * @param {String} config.channel channel
   * @param {Object} config.props additional attributes to add to iframe element
   * @param {Object} bindings RPC method stubs and implementations
   * @param {Object} bindings.local local function implementations - functions that exist in the current context.
   *    XMLRPC exposes these functions so that they can be invoked by code running in the other side of the iframe.
   * @param {Array} bindings.remote names of functions which exist on the other side of the iframe.
   *    XMLRPC creates stubs to these functions that can be invoked from the current page.
   * @returns XDM-RPC instance
   * @constructor
   */
  function XdmRpc(config, bindings) {

    var self, id = (count += 1),
      target, remoteOrigin, channel, mixin,
      locals = bindings.local || {},
      remotes = bindings.remote || [];

    // A hub through which all async callbacks for remote requests are parked until invoked from a response
    var nexus = function () {
      var callbacks = {};
      return {
        // Registers a callback of a given type by uid
        add: function (type, uid, callback) {
          callbacks[uid] = callbacks[uid] || {};
          callbacks[uid][type] = callback || null;
        },
        // Invokes callbacks for a response of a given type by uid if registered, then removes all handlers for the uid
        invoke: function (type, uid, arg) {
          var complete;
          if (callbacks[uid]) {
            if (callbacks[uid][type]) callbacks[uid][type](arg);
            complete = true;
            delete callbacks[uid];
          }
          return complete;
        }
      };
    }();

    // The xdm_e query param is set on add-on iframe urls
    if (!/xdm_e/.test(loc)) {
      // Host-side constructor branch
      var iframe = createIframe(config);
      target = iframe.contentWindow;
      remoteOrigin = getBaseUrl(config.remote);
      channel = config.channel;
      mixin = {
        isHost: true,
        iframe: iframe,
        destroy: function () {
          unbind();
          if (self.iframe) {
            $(self.iframe).remove();
            delete self.iframe;
          }
        },
        isActive: function () {
          // Host-side instances are only active as long as the iframe they communicate with still exists in the DOM
          return $.contains(document.documentElement, self.iframe);
        }
      };
    } else {
      // Add-on-side constructor branch
      target = window.parent;
      remoteOrigin = param(loc, "xdm_e");
      channel = param(loc, "xdm_c");
      mixin = {
        isActive: function () {
          // Add-on-side instances are always active, as they must always have a parent window peer
          return true;
        }
      };
    }

    // Create the actual XdmRpc instance, and apply the context-sensitive mixin
    self = $.extend({
      id: id,
      remoteOrigin: remoteOrigin,
      channel: channel
    }, mixin);

    // Sends a message of a specific type to the remote peer via a post-message event
    function send(sid, type, message) {
      try {
        target.postMessage(JSON.stringify({
          c: channel,
          i: sid,
          t: type,
          m: message
        }), remoteOrigin);
      } catch (ex) {
        log(errmsg(ex));
      }
    }

    // Sends a request with a specific remote method name, args, and optional callbacks
    function sendRequest(methodName, args, done, fail) {
      // Generate a random ID for this remote invocation
      var id = Math.floor(Math.random() * 1000000000).toString(16);
      // Register any callbacks with the nexus so they can be invoked when a response is received
      nexus.add("done", id, done);
      nexus.add("fail", id, fail);
      // Send a request to the remote, where:
      //  - n is the name of the remote function
      //  - a is an array of the (hopefully) serializable, non-callback arguments to this method
      send(id, "request", {n: methodName, a: args});
    }

    function sendDone(id, message) {
      send(id, "done", message);
    }

    function sendFail(id, message) {
      send(id, "fail", message);
    }

    // Handles an normalized, incoming post-message event
    function receive(e) {
      try {
        // Extract message payload from the event
        var payload = JSON.parse(e.data),
            pid = payload.i, pchannel = payload.c, ptype = payload.t, pmessage = payload.m;
        // If the payload doesn't match our expected event signature, assume its not part of the xdm-rpc protocol
        if (e.source !== target || e.origin !== remoteOrigin || pchannel !== channel) return;
        if (ptype === "request") {
          // If the payload type is request, this is an incoming method invocation
          var name = pmessage.n, args = pmessage.a,
              local = locals[name], done, fail, async;
          if (local) {
            // The message name matches a locally defined RPC method, so inspect and invoke it according
            // Create responders for each response type
            done = function (message) { sendDone(pid, message); };
            fail = function (message) { sendFail(pid, message); };
            // The local method is considered async if it accepts more arguments than the message has sent;
            // the additional arguments are filled in with the above async responder callbacks;
            // TODO: consider specifying args somehow in the remote stubs so that non-callback args can be
            //       verified before sending a request to fail fast at the callsite
            async = (args ? args.length : 0) < local.length;
            try {
              if (async) {
                // If async, apply the method with the responders added to the args list
                local.apply(locals, args.concat([done, fail]));
              } else {
                // Otherwise, immediately respond with the result
                done(local.apply(locals, args));
              }
            } catch (ex) {
              // If the invocation threw an error, invoke the fail responder callback with it
              fail(errmsg(ex));
            }
          } else {
            // No such local rpc method name found
            debug("Unhandled request:", payload);
          }
        } else if (ptype === "done" || ptype === "fail") {
          // The payload is of a response type, so try to invoke the appropriate callback via the nexus registry
          if (!nexus.invoke(ptype, pid, pmessage)) {
            // The nexus didn't find an appropriate reponse callback to invoke
            debug("Unhandled response:", ptype, pid, pmessage);
          }
        }
      } catch (ex) {
        log(errmsg(ex));
      }
    }

    // Creates a bridging invocation function for a remote method
    function bridge(methodName) {
      // Add a method to this instance that will convert from 'rpc.method(args..., done?, fail?)'-style
      // invocations to a postMessage event via the 'send' function
      return function () {
        var args = slice(arguments), done, fail;
        // Pops the last arg off the args list if it's a function
        function popFn() {
          if (isFn(args[args.length - 1])) {
            return args.pop();
          }
        }
        // Remove done/fail callbacks from the args list
        fail = popFn();
        done = popFn();
        if (!done) {
          // Set the done cb to the value of the fail cb if only one callback fn was given
          done = fail;
          fail = undefined;
        }
        sendRequest(methodName, args, done, fail);
      };
    }

    // For each remote method, generate a like-named interceptor on this instance that converts invocations to
    // post-message request events, tracking async callbacks as necessary.
    $.each(remotes, function (methodName, v) {
      // If remotes were specified as an array rather than a map, promote v to methodName
      if (typeof methodName === "number") methodName = v;
      self[methodName] = bridge(methodName);
    });

    // Create and attach a local event emitter for bridged pub/sub
    var events = self.events = new Bus();
    // Attach an any-listener to forward all locally-originating events to the remote peer
    events.onAny(function () {
      var args = slice(arguments);
      // The actual event object is the last argument passed to any listener
      var event = args[args.length - 1];
      // If the event originated locally or this is the host-side and the event didn't originate from the remote peer,
      // then forward it to the remote peer
      var eventOrigin = event.origin;
      if (eventOrigin === localOrigin || (self.isHost && eventOrigin !== remoteOrigin)) {
        debug("Firing remote event:", event);
        sendRequest("_event", event);
      }
    });
    // Define our own reserved local to receive remote events
    locals._event = function (event) {
      debug("Received remote event:", event);
      events._emitEvent(event);
    };

    // Handles incoming postMessages from this XdmRpc instance's remote peer
    function postMessageHandler(e) {
      if (self.isActive()) {
        // Normalize and forward the event message to the receiver logic
        receive(e.originalEvent ? e.originalEvent : e);
      } else {
        // If inactive (due to the iframe element having disappeared from the DOM), force cleanup of this callback
        unbind();
      }
    }

    // Starts listening for window messaging events
    function bind() {
      $window.bind("message", postMessageHandler);
    }

    // Stops listening for window messaging events
    function unbind() {
      $window.unbind("message", postMessageHandler);
    }

    // Immediately start listening for events
    bind();

    return self;
  }

  // A simple event bus
  var Bus = (function () {
    function Bus() {
      this._events = {};
      this._any = [];
    }

    var proto = Bus.prototype;

    // Returns an array of listeners by name
    proto.listeners = function (name) {
      return this._events[name] = this._events[name] || [];
    };

    // Adds a listener for an event by name
    proto.on = function (name, listener) {
      if (name && listener) {
        this.listeners(name).push(listener);
      }
      return this;
    };

    // Adds a listener for an event by name, removing the listener when fired
    proto.once = function (name, listener) {
      var self = this;
      var interceptor = function () {
        self.off(name, interceptor);
        listener.apply(null, arguments);
      };
      this.on(name, interceptor);
      return this;
    };

    // Adds a listener that fires on any event
    proto.onAny = function (listener) {
      this._any.push(listener);
      return this;
    };

    // Removes a listener for an event by name
    proto.off = function (name, listener) {
      var all = this._events[name];
      if (all) {
        var i = all.indexOf(listener);
        if (i >= 0) {
          all.splice(i, 1);
        }
        if (all.length === 0) {
          delete this._events[name];
        }
      }
      return this;
    };

    // Removes all listeners for an event by name, or all (non-any) listeners if name is undefined
    proto.offAll = function (name) {
      if (name) {
        delete this._events[name];
      } else {
        this._events = {};
      }
      return this;
    };

    // Removes a listener that fires on any event
    proto.offAny = function (listener) {
      var any = this._any;
      var i = any.indexOf(listener);
      if (i >= 0) {
        any.splice(i, 1);
      }
      return this;
    };

    // Returns an array containing all event names with listeners, or an empty array
    proto.active = function () {
      // Not using $.map here since it's not implemented in the $-shim on the iframe side
      var names = [];
      $.each(this._events, function (k, v) {
        if (v && v.length > 0) names.push(k);
      });
      return names;
    };

    // Internal helper for firing an event to an array of listeners
    function fire(listeners, event) {
      var args = event.args.concat([event]);
      $.each(listeners, function () {
        try {
          this.apply(null, args);
        } catch (e) {
          log(e);
        }
      });
    }

    // Emits an event on this bus, firing listeners by name as well as the any listener; arguments following the
    // name parameter are captured and passed to listeners
    proto.emit = function (name) {
      return this._emitEvent(name, {
        name: name,
        args: slice(arguments, 1),
        origin: localOrigin
      });
    };

    // An internal method for emitting event objects
    proto._emitEvent = function (name, event) {
      fire(this.listeners(name), event);
      fire(this._any, event);
      return this;
    };

    return Bus;
  })();

  // Crudely extracts a query param value from a url by name
  function param(url, name) {
    return decodeURIComponent(RegExp(name + "=([^&]+)").exec(url)[1]);
  }

  // Determines a base url consisting of protocol+domain+port from a given url string
  function getBaseUrl(url) {
    var m = url.toLowerCase().match(/^((http.?:)\/\/([^:\/\s]+)(:\d+)*)/),
      proto = m[2], domain = m[3], port = m[4] || "";
    if ((proto === "http:" && port === ":80") || (proto === "https:" && port === ":443")) port = "";
    return proto + "//" + domain + port;
  }

  // Appends a map of query parameters to a base url
  function toUrl(base, params) {
    var url = base, sep = /\?/.test(base) ? "&" : "?";
    $.each(params, function (k, v) {
      url += sep + encodeURIComponent(k) + "=" + encodeURIComponent(v);
      sep = "&";
    });
    return url;
  }

  // Creates an iframe element from a config option consisting of the following values:
  //  - container:  the parent element of the new iframe
  //  - remote:     the src url of the new iframe
  //  - props:      a map of additional HTML attributes for the new iframe
  //  - channel:    deprecated
  function createIframe(config) {
    var iframe = document.createElement("iframe"),
        id = "easyXDM_" + config.container + "_provider";
    var src = toUrl(config.remote, {
      xdm_e: localOrigin,
      // for signing compat until server is changed to omit it
      xdm_c: config.channel,
      xdm_p: 1
    });
    $.extend(iframe, {id: id, name: id, frameBorder: "0"}, config.props);
    $("#" + config.container).append(iframe);
    iframe.src = src;
    return iframe;
  }

  // Extracts a displayable message from an error or other object
  function errmsg(ex) {
    return ex.message || ex.toString();
  }

  // Logs its arguments as debug messages when the XdmRpc.debug flag is set to a truthy value
  function debug() {
    if (XdmRpc.debug) log.apply(window, ["DEBUG:"].concat(slice(arguments)));
  }

  // Logs its arguments to the best available log output
  function log() {
    var w = window, log = $.log || (w.AJS && w.AJS.log);
    if (log) log.apply(w, arguments);
  }

  function slice(o, s, e) {
    var len = o.length >>> 0;
    return len > 0 ? [].slice.call(o, s || 0, e || len) : [];
  }

  return XdmRpc;
});
