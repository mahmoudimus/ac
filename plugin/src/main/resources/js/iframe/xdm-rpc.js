(function (window, document, encode, decode, console, JSON, AJS, undefined) {

  "use strict";

  var AP = window._AP || window.AP,
      loc = window.location.toString(),
      count = 0;

  function factory($) {

    var $window = $(window),
        isFn = $.isFunction;

    function param(url, name) {
      return decode(RegExp(name + "=([^&]+)").exec(url)[1]);
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
        url += sep + encode(k) + "=" + encode(v);
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

  }

  if (AP.define) {
    AP.define("_xdm-rpc", ["_dollar"], factory);
  }
  else {
    AP._XdmRpc = AP._XdmRpc || factory(AJS.$);
  }

}(this, document, encodeURIComponent, decodeURIComponent, this.console, JSON, this.AJS));
