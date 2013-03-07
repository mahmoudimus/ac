(function (window, document, console) {

  var AP = window.RA = window.AP = {}; // RA is deprecated

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
      el[add](e, fn, false);
    }
    else if (el[attach]) {
      el[attach]("on" + e, fn);
    }
  }

  // string trimmer
  function trim(s) {
    return s && s.replace(/^\s+|\s+$/g, "");
  }

  // no-op
  function nop() {}

  // basic dom util
  function $(sel, context) {
    context = context || document;
    var els = [];
    if (sel) {
      if (typeof sel === "string") {
        var results = context.querySelectorAll(sel);
        each(results, function (i, v) { els.push(v); });
      }
      else if (sel.nodeType === 1) {
        els.push(sel);
      }
      else if (sel === window) {
        els.push(sel);
      }
    }
    extend(els, {
      each: function (it) {
        each(this, it);
        return this;
      },
      bind: function (name, callback) {
        this.each(function (i, el) {
          bind(el, name, callback);
        });
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
        (window.ActiveXObject && new ActiveXObject("Microsoft.XMLHTTP")) ||
        (window.XMLHttpRequest && new XMLHttpRequest());
    xhr.onreadystatechange = function () {
      if (xhr.readyState === 4) {
        var status = xhr.status,
            response = xhr.responseText,
            contentType = xhr.getResponseHeader("Content-Type"),
            success = options.success || nop,
            error = options.error || nop,
            body;
        if (status >= 200 && status <= 300) {
          if (contentType && contentType.indexOf("application/json") === 0) {
            try {
              body = JSON.parse(response);
              try {
                success(body, xhr.statusText);
              }
              catch (ex) {
                handleError(ex);
              }
            }
            catch (ex) {
              error(xhr, "parseerror", ex);
            }
          }
          else {
            success(response, xhr.statusText);
          }
        }
        else {
          error(xhr, xhr.statusText || "error");
        }
      }
    };
    xhr.open("GET", options.url, true);
    var headers = options.headers || {};
    headers["AP-Auth-State"] = AP.Meta.get("auth-state");
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

  function log() {
    if (console) {
      console.log.apply(console, arguments);
    }
  }

  function handleError(err) {
    if (console) {
      console.error.apply(console, [err.message, err] || [err]);
    }
    else {
      throw err;
    }
  }

  AP._$ = extend($, {
    each: each,
    extend: extend,
    bind: bind,
    trim: trim,
    fetch: fetch,
    debounce: debounce,
    log: log,
    handleError: handleError
  });

}(this, document, this.console));
