(window.AP || window._AP).define("_xdm-rpc", ["_dollar"], function($) {

  "use strict";

  var loc = window.location.toString(),
    count = 0;

  var $window = $(window),
      isFn = $.isFunction;

  function param(url, name) {
    return decodeURIComponent(RegExp(name + "=([^&]+)").exec(url)[1]);
  }

  function getBaseUrl(url) {
    var m = url.toLowerCase().match(/^((http.?:)\/\/([^:\/\s]+)(:\d+)*)/),
        proto = m[2], domain = m[3], port = m[4] || "";
    if ((proto === "http:" && port === ":80") || (proto === "https:" && port === ":443")) port = "";
    return proto + "//" + domain + port;
  }

  function toUrl(base, params) {
    var url = base, sep = /\?/.test(base) ? "&" : "?";
    $.each(params, function (k, v) {
      url += sep + encodeURIComponent(k) + "=" + encodeURIComponent(v);
      sep = "&";
    });
    return url;
  }

  function createIframe(config) {
    var iframe = document.createElement("iframe"),
        id = "easyXDM_" + config.container + "_provider";
    var src = toUrl(config.remote, {
      xdm_e: getBaseUrl(loc),
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
    if (XdmRpc.debug) log.apply(window, ["DEBUG:"].concat([].slice.call(arguments)));
  }

  function log() {
    var log = $.log || (window.AJS && window.AJS.log);
    if (log) {
      log.apply(window, arguments);
      return true;
    }
  }

  /**
   * Sets up cross-iframe remote procedure calls.
   * If this is called from a parent window, iframe is created and a RPC interface for communicating with it is set up.
   * If this is called from within the iframe, and RPC interface for communicating with the parent is set up.
   *
   * Calling a remote function is done with the signature:
   *     fn(data..., doneCallback, failCallback)
   * doneCallback is called after the remote function executed successfully.
   * failCallback is called after the remote function throws an exception
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
        target, origin, channel, mixin,
        locals = bindings.local || {},
        remotes = bindings.remote || [];

    var nexus = function () {
      function handler() {
        var callbacks = {};
        return function (id, callbackOrArg) {
          if (isFn(callbackOrArg)) {
            callbacks[id] = callbackOrArg;
          }
          else if (callbacks[id]) {
            callbacks[id](callbackOrArg);
            delete callbacks[id];
          }
        };
      }
      return {
        done: handler(),
        fail: handler()
      };
    }();

    if (!/xdm_e/.test(loc)) {
      var iframe = createIframe(config);
      target = iframe.contentWindow;
      origin = getBaseUrl(config.remote);
      channel = config.channel;
      mixin = {
        iframe: iframe,
        destroy: function () {
          unbind();
          if (self.iframe) {
            $(self.iframe).remove();
            delete self.iframe;
          }
        },
        isActive: function () {
          return $.contains(document.documentElement, self.iframe);
        }
      };
    }
    else {
      target = window.parent;
      origin = param(loc, "xdm_e");
      channel = param(loc, "xdm_c");
      mixin = {
        isActive: function () {
          return true;
        }
      };
    }

    self = $.extend({
      id: id,
      origin: origin,
      channel: channel
    }, mixin);

    function send(sid, type, message) {
      try {
        target.postMessage(JSON.stringify({
          c: channel,
          i: sid,
          t: type,
          m: message
        }), origin);
      }
      catch (ex) {
        log(errmsg(ex));
      }
    }

    function receive(e) {
      function responder(state) {
        return function (message) {
          send(pid, state, message);
        }
      }
      try {
        var payload = JSON.parse(e.data),
            pid = payload.i, pchannel = payload.c, ptype = payload.t, pmessage = payload.m;
        if (e.source !== target || e.origin !== origin || pchannel !== channel) return;
        if (ptype === "request") {
          var name = pmessage.n, args = pmessage.a,
              local = locals[name], done, fail, async;
          if (local) {
            done = responder("done");
            fail = responder("fail");
            async = (args ? args.length : 0) < local.length;
            try {
              if (async) {
                local.apply(locals, args.concat([done, fail]));
              }
              else {
                done(local.apply(locals, args));
              }
            }
            catch (ex) {
              fail(errmsg(ex));
            }
          }
          else {
            debug("Unhandled request:", payload);
          }
        }
        else if (ptype === "done" || ptype === "fail") {
          var handler = nexus[ptype];
          if (handler) handler(pid, pmessage);
          else debug("Unhandled response:", payload);
        }
      }
      catch (ex) {
        log(errmsg(ex));
      }
    }

    $.each(remotes, function (k, v) {
      if (typeof k === "number") k = v; // switch k to value if remotes specified in array
      self[k] = function () {
        var args = [].slice.call(arguments), done, fail;
        function popFn() {
          if (isFn(args[args.length - 1])) {
            return args.pop();
          }
        }
        fail = popFn();
        done = popFn();
        if (!done) {
          done = fail;
          fail = undefined;
        }
        var id = Math.floor(Math.random() * 1000000000).toString(16);
        if (done) nexus.done(id, done);
        if (fail) nexus.fail(id, fail);
        send(id, "request", {n: k, a: args});
      };
    });

    function postMessageHandler(e) {
      if (self.isActive()) {
        receive(e.originalEvent ? e.originalEvent : e);
      }
      else {
        unbind();
      }
    }

    function bind() {
      $window.bind("message", postMessageHandler);
    }

    function unbind() {
      $window.unbind("message", postMessageHandler);
    }

    bind();

    return self;
  }
  return XdmRpc;
});
