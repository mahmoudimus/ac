(function (window, document, JSON, encode, decode, undefined) {

  var AP = window.AP || window._AP,
      $ = AP._$ || (window.AJS && AJS.$) || window.jQuery,
      loc = window.location.toString();

  AP._Rpc = function (config, bindings) {
    var self = {}, target, origin, channel,
        locals = bindings.local || {},
        remotes = bindings.remote || [];

    var nexus = function () {
      function handler() {
        var callbacks = {};
        return function (id, callbackOrArg) {
          if (typeof callbackOrArg === "function") {
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
      // host
      var iframe = createIframe(config);
      target = iframe.contentWindow;
      origin = getBaseUrl(config.remote);
      channel = config.channel;
      self.destroy = function () {
        // @todo remove iframe
        // @todo unbind listener
      };
    }
    else {
      // plugin
      target = window.parent;
      origin = param(loc, "xdm_e");
      channel = param(loc, "xdm_c");
    }

    function send(id, type, message) {
      try {
        target.postMessage(JSON.stringify({
          c: channel,
          i: id,
          t: type,
          m: message
        }), origin);
      }
      catch (ex) {
        log(errmsg(ex));
      }
    }

    function receive(e) {
      try {
        var payload = JSON.parse(e.data),
            pid = payload.i, pchannel = payload.c, ptype = payload.t, pmessage = payload.m;
        if (e.source !== target || e.origin !== origin || pchannel !== channel) return;
        if (ptype === "request") {
          var name = pmessage.n, args = pmessage.a,
              local = locals[name], done, fail, async;
          if (local) {
            function responder(state) {
              return function (message) {
                send(pid, state, message);
              }
            }
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
            log("Unhandled request:", payload);
          }
        }
        else if (ptype === "done" || ptype === "fail") {
          var handler = nexus[ptype];
          if (handler) handler(pid, pmessage);
          else log("Unhandled response:", payload);
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
          if (typeof args[args.length - 1] === "function") return args.pop();
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

    $(window).bind("message", function (e) {
      receive(e.originalEvent ? e.originalEvent : e);
    });

    return self;
  };

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
    var iframe = document.createElement("iframe");
    var id = "easyXDM_" + config.container + "_provider";
    var src = toUrl(config.remote, {
      xdm_e: getBaseUrl(loc),
      // for signing compat until server is changed to omit it
      xdm_c: config.channel,
      xdm_p: 1
    });
    $.extend(iframe, {id: id, name: id, frameBorder: "0"}, config.props);
    var container = $("#" + config.container)[0];
    container.appendChild(iframe);
    iframe.src = src;
    return iframe;
  }

  function errmsg(ex) {
    return ex.message || ex.toString();
  }

  function log() {
    if (window.console) console.log.apply(console, arguments);
  }

}(this, document, JSON, encodeURIComponent, decodeURIComponent));
