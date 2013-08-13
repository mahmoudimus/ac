(this.AP || this._AP).define("_xdm", ["_dollar", "_events"], function ($, events) {

  "use strict";

  // Capture some common values and symbol aliases
  var w = window,
      $w = $(w),
      loc = w.location.toString(),
      localOrigin = getBaseUrl(loc),
      count = 0,
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
   * @param {String} config.remoteKey The remote peer's add-on key (host only)
   * @param {String} config.remote The src of remote iframe (host only)
   * @param {String} config.container The id of element to which the generated iframe is appended (host only)
   * @param {Object} config.props Additional attributes to add to iframe element (host only)
   * @param {String} config.channel Channel (host only); deprecated
   * @param {Object} bindings RPC method stubs and implementations
   * @param {Object} bindings.local Local function implementations - functions that exist in the current context.
   *    XdmRpc exposes these functions so that they can be invoked by code running in the other side of the iframe.
   * @param {Array} bindings.remote Names of functions which exist on the other side of the iframe.
   *    XdmRpc creates stubs to these functions that can be invoked from the current page.
   * @returns XdmRpc instance
   * @constructor
   */
  function XdmRpc(config, bindings) {

    var self, id = "" + (count += 1),
        target, remoteOrigin, channel, mixin,
        localKey, remoteKey,
        locals = bindings.local || {},
        remotes = bindings.remote || [];

    // A hub through which all async callbacks for remote requests are parked until invoked from a response
    var nexus = function () {
      var callbacks = {};
      return {
        // Registers a callback of a given type by uid
        add: function (uid, done, fail) {
          callbacks[uid] = {};
          callbacks[uid].done = done || null;
          callbacks[uid].fail = fail || null;
          callbacks[uid].async = !!done;
        },
        // Invokes callbacks for a response of a given type by uid if registered, then removes all handlers for the uid
        invoke: function (type, uid, arg) {
          var handled;
          if (callbacks[uid]) {
            if (callbacks[uid][type]) {
              // If the intended callback exists, invoke it and mark the response as handled
              callbacks[uid][type](arg);
              handled = true;
            } else {
              // Only mark other calls as handled if they weren't expecting a callback and didn't fail
              handled = !callbacks[uid].async && type !== "fail";
            }
            delete callbacks[uid];
          }
          return handled;
        }
      };
    }();

    // The xdm_e query param is set on add-on iframe urls
    if (!/xdm_e/.test(loc)) {
      // Host-side constructor branch
      var iframe = createIframe(config);
      target = iframe.contentWindow;
      localKey = param(config.remote, "oauth_consumer_key");
      remoteKey = config.remoteKey;
      remoteOrigin = getBaseUrl(config.remote);
      channel = config.channel;
      // Define the host-side mixin
      mixin = {
        isHost: true,
        iframe: iframe,
        destroy: function () {
          // Unbind postMessage handler when destroyed
          unbind();
          // Then remove the iframe, if it still exists
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
      target = w.parent;
      localKey = "local"; // Would be better to make this the add-on key, but it's not readily available at this time
      remoteKey = param(loc, "oauth_consumer_key");
      remoteOrigin = param(loc, "xdm_e");
      channel = param(loc, "xdm_c");
      // Define the add-on-side mixin
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
      nexus.add(id, done, fail);
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
        var args = [].slice.call(arguments), done, fail;
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
    var bus = self.events = new events.Events(localKey, localOrigin);
    // Attach an any-listener to forward all locally-originating events to the remote peer
    bus.onAny(function () {
      var args = [].slice.call(arguments);
      // The actual event object is the last argument passed to any listener
      var event = args[args.length - 1];
      // If the event originated locally or this is the host-side and the event didn't originate from the remote peer,
      // then forward it to the remote peer
      var eventOrigin = event.source.origin;
      if (eventOrigin === localOrigin || (self.isHost && eventOrigin !== remoteOrigin)) {
        debug("Forwarding local event:", event);
        sendRequest("_event", [event]);
      }
    });
    // Define our own reserved local to receive remote events
    locals._event = function (event) {
      if (self.isHost) {
        console.log('hey', arguments);
        // When the running on the host-side, forcibly reset the event's key and origin fields, to prevent spoofing by
        // untrusted add-ons; also include the host-side XdmRpc instance id to tag the event with this particular
        // instance of the host/add-on relationship
        event.source = {
          id: id,
          key: remoteKey,
          origin: remoteOrigin
        };
      }
      debug("Receiving remote event:", event);
      // Emit the event on the local bus
      bus._emitEvent(event);
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
      $w.bind("message", postMessageHandler);
    }

    // Stops listening for window messaging events
    function unbind() {
      $w.unbind("message", postMessageHandler);
    }

    // Immediately start listening for events
    bind();

    return self;
  }

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

  function errmsg(ex) {
    return ex.message || ex.toString();
  }

  function debug() {
    if (XdmRpc.debug) log.apply(w, ["DEBUG:"].concat([].slice.call(arguments)));
  }

  function log() {
    var log = $.log || (w.AJS && w.AJS.log);
    if (log) log.apply(w, arguments);
  }

  // DEBUG
  XdmRpc.debug = true;

  return XdmRpc;

});
