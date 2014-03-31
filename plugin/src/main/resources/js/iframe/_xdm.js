(this.AP || this._AP).define("_xdm", ["_events", "_base64", "_uri", "analytics"], function (events, base64, uri, analytics) {

  "use strict";

  // Capture some common values and symbol aliases
  var w = window,
      loc = w.location.toString(),
      count = 0;

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
   * @param {Object} $ jquery or jquery-like utility
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
  function XdmRpc($, config, bindings) {

    var self, id, target, remoteOrigin, channel, mixin,
        localKey, remoteKey, addonKey,
        locals = bindings.local || {},
        remotes = bindings.remote || [],
        localOrigin = getBaseUrl(loc);

    // A hub through which all async callbacks for remote requests are parked until invoked from a response
    var nexus = function () {
      var callbacks = {};
      return {
        // Registers a callback of a given type by uid
        add: function (uid, done, fail) {
          callbacks[uid] = {
            done: done || null,
            fail: fail || null,
            async: !!done
          };
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

    // Use the config and enviroment to construct the core of the new XdmRpc instance.
    //
    // Note: The xdm_e|c|p variables that appear in an iframe URL are used to pass message to the XdmRpc bridge
    // when running inside an add-on iframe.  Their names are holdovers from easyXDM, which was used prior
    // to building this proprietary library (which was done both to greatly reduce the total amount of JS
    // needed to drive the postMessage-based RPC communication, and to allow us to extend its capabilities).
    //
    // AC-451 describes how we can reduce/improve these (and other) iframe url parameters, but until that is
    // addressed, here's a brief description of each:
    //
    //  - xdm_e contains the base url of the host app; it's presence indicates that the XdmRpc is running in
    //    an add-on iframe
    //  - xdm_c contains a unique channel name; this is a holdover from easyXDM that was used to distinguish
    //    postMessage events between multiple iframes with identical xdm_e values, though this may now be
    //    redundant with the current internal implementation of the XdmRpc and should be considered for removal
    if (!/xdm_e/.test(loc)) {
      // Host-side constructor branch
      var iframe = createIframe(config);
      target = iframe.contentWindow;
      localKey = param(config.remote, "oauth_consumer_key");
      remoteKey = config.remoteKey;
      addonKey = remoteKey;
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

      // identify the add-on by unique key: first try JWT issuer claim and fall back to OAuth1 consumer key
      var jwt = param(loc, "jwt");
      remoteKey = jwt ? parseJwtIssuer(jwt) : param(loc, "oauth_consumer_key");

      // if the authentication method is "none" then it is valid to have no jwt and no oauth in the url
      // but equally we don't trust this iframe as far as we can throw it, so assign it a random id
      // in order to prevent it from talking to any other iframe
      if (null == remoteKey) {
          remoteKey = Math.random(); // unpredictable and unsecured, like an oauth consumer key
      }

      addonKey = localKey;
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

    id = addonKey + "|" + (count += 1);

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
      var sid = Math.floor(Math.random() * 1000000000).toString(16);
      // Register any callbacks with the nexus so they can be invoked when a response is received
      nexus.add(sid, done, fail);
      // Send a request to the remote, where:
      //  - n is the name of the remote function
      //  - a is an array of the (hopefully) serializable, non-callback arguments to this method
      send(sid, "request", {n: methodName, a: args});
    }

    function sendDone(sid, message) {
      send(sid, "done", message);
    }

    function sendFail(sid, message) {
      send(sid, "fail", message);
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

            //analytics
            if(self.isHost){
              analytics.trackBridgeMethod(name, addonKey, channel);
            }

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
          if ($.isFunction(args[args.length - 1])) {
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
      // The actual event object is the last argument passed to any listener
      var event = arguments[arguments.length - 1];
      var trace = event.trace = event.trace || {};
      var traceKey = id + "|xdm";
      if ((self.isHost && !trace[traceKey] && event.source.channel !== id)
          || (!self.isHost && event.source.key === localKey)) {
        // Only forward an event once in this listener
        trace[traceKey] = true;
        // Clone the event and forward without tracing info, to avoid leaking host-side iframe topology to add-ons
        event = $.extend({}, event);
        delete event.trace;
        debug("Forwarding " + (self.isHost ? "host" : "addon") + " event:", event);
        sendRequest("_event", [event]);
      }
    });
    // Define our own reserved local to receive remote events
    locals._event = function (event) {
      // Reset/ignore any tracing info that may have come across the bridge
      delete event.trace;
      if (self.isHost) {
        // When the running on the host-side, forcibly reset the event's key and origin fields, to prevent spoofing by
        // untrusted add-ons; also include the host-side XdmRpc instance id to tag the event with this particular
        // instance of the host/add-on relationship
        event.source = {
          channel: id, // Note: the term channel here != the deprecated xdm channel param
          key: remoteKey,
          origin: remoteOrigin
        };
      }
      debug("Receiving " + (self.isHost ? "addon" : "host") + " event:", event);
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
      $(window).bind("message", postMessageHandler);
    }

    // Stops listening for window messaging events
    function unbind() {
      $(window).unbind("message", postMessageHandler);
    }

    // Crudely extracts a query param value from a url by name
    function param(url, name) {
      return new uri.init(url).getQueryParamValue(name);
    }

    // Determines a base url consisting of protocol+domain+port from a given url string
    function getBaseUrl(url) {
      return new uri.init(url).origin();
    }

    // Appends a map of query parameters to a base url
    function toUrl(base, params) {
      var url = new uri.init(base);
      $.each(params, function (k, v) {
        url.addQueryParam(k,v);
      });
      return url.toString();
    }

    // Creates an iframe element from a config option consisting of the following values:
    //  - container:  the parent element of the new iframe
    //  - remote:     the src url of the new iframe
    //  - props:      a map of additional HTML attributes for the new iframe
    //  - channel:    deprecated
    function createIframe(config) {
      var iframe = document.createElement("iframe"),
        id = "easyXDM_" + config.container + "_provider";
      $.extend(iframe, {id: id, name: id, frameBorder: "0"}, config.props);
      //$.extend will not add the attribute rel.
      iframe.setAttribute('rel', 'nofollow');
      $("#" + config.container).append(iframe);
      iframe.src = config.remote;
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

    function parseJwtIssuer(jwt) {
      return parseJwtClaims(jwt)['iss'];
    }

    function parseJwtClaims(jwt) {

      if (null == jwt || '' == jwt) {
        throw('Invalid JWT: must be neither null nor empty-string.');
      }

      var firstPeriodIndex = jwt.indexOf('.');
      var secondPeriodIndex = jwt.indexOf('.', firstPeriodIndex + 1);

      if (firstPeriodIndex < 0 || secondPeriodIndex <= firstPeriodIndex) {
        throw('Invalid JWT: must contain 2 period (".") characters.');
      }

      var encodedClaims = jwt.substring(firstPeriodIndex + 1, secondPeriodIndex);

      if (null == encodedClaims || '' == encodedClaims) {
        throw('Invalid JWT: encoded claims must be neither null nor empty-string.');
      }

      var claimsString = base64.decode(encodedClaims);
      return JSON.parse(claimsString);
    }

    // Immediately start listening for events
    bind();

    return self;
  }

//  XdmRpc.debug = true;

  return XdmRpc;

});
