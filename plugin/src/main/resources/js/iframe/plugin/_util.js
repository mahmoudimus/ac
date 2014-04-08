AP.define("_util", function () {

  "use strict";

  // universal iterator utility
  function each(o, it) {
    var l, k;
    if (o) {
      l = o.length;
      if (l != null && typeof o !== "function") {
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

  function binder(std, odd) {
    std += "EventListener";
    odd += "Event";
    return function (el, e, fn) {
      if (el[std]) {
        el[std](e, fn, false);
      }
      else if (el[odd]) {
        el[odd]("on" + e, fn);
      }
    };
  }

  function log() {
    var console = this.console;
    if (console && console.log) {
      var args = [].slice.call(arguments);
      if (console.log.apply) {
        console.log.apply(console, args);
      }
      else {
        for (var i = 0, l = args.length; i < l; i += 1) {
          args[i] = JSON.stringify(args[i]);
        }
        console.log(args.join(" "));
      }
      return true;
    }
  }

  function decodeQueryComponent(encodedURI) {
    return encodedURI == null ? null : decodeURIComponent( encodedURI.replace(/\+/g, '%20') );
  }

  return {

    each: each,

    extend: function (dest) {
      var args = arguments,
          srcs = [].slice.call(args, 1, args.length);
      each(srcs, function (i, src) {
        each(src, function (k, v) {
          dest[k] = v;
        });
      });
      return dest;
    },

    bind: binder("add", "attach"),

    unbind: binder("remove", "detach"),

    trim: function (s) {
      return s && s.replace(/^\s+|\s+$/g, "");
    },

    debounce: function (fn, wait) {
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
    },

    inArray: function (value, array, fromIndex) {
      //optimisation for all browsers after IE8
      if (Array.prototype.indexOf) {
        return Array.prototype.indexOf.call(array, value, fromIndex);
      }

      var k = fromIndex >>> 0, len = array.length >>> 0;
      for (; k < len; k += 1) {
        if (array[k] === value) return k;
      }
      return -1;
    },

    isFunction: function (fn) {
      return typeof fn === "function";
    },

    log: log,

    handleError: function (err) {
      if (!log.apply(this, err && err.message ? [err, err.message] : [err])) {
        throw err;
      }
    },

    decodeQueryComponent: decodeQueryComponent
  };

});
