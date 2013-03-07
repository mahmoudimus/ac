(function (window, document) {

  var AP = window.AP,
      $ = AP._$,
      each = $.each,
      extend = $.extend,
      bind = $.bind,
      trim = $.trim,
      fetch = $.fetch,
      debounce = $.debounce,
      log = $.log,
      handleError = $.handleError,
      rpc,
      isDialog,
      isInited;

  AP.Meta = {
    get: function (name) {
      return $("meta[name='ap-" + name + "']").attr("content");
    }
  };

  AP.localUrl = function (path) {
    return AP.Meta.get("local-base-url") + (path == null ? "" : path);
  };

  AP.BigPipe = function () {
    function BigPipe() {
      var started,
          closed,
          channels = {},
          subscribers = {},
          buffers = {},
          counter = 0;
      function poll(url) {
        fetch({
          url: url + "/" + (counter += 1),
          headers: {"Accept": "application/json"},
          success: function (response) {
            deliver(response, url);
          },
          error: function (xhr, status, ex) {
            if (xhr.status === 404) {
              // despite being a real error, treat a 404 as a termination signal
              deliver({items: [], pending: []}, url);
            }
            else {
              handleError(ex || (xhr && xhr.responseText) || status);
            }
          }
        });
      }
      function deliver(response, url) {
        if (response) {
          var items = response.items;
          var pending = response.pending;
          if (items.length > 0 || pending.length > 0) {
            each(items, function (i, item) {
              publish(item);
            });
            if (pending.length > 0) {
              each(channels, function (channelId, open) {
                if (open && pending.indexOf(channelId) < 0) {
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
          var baseUrl = options.localBaseUrl || AP.Meta.get("local-base-url");
          var requestId = options.requestId;
          if (!started && baseUrl && requestId) {
            var ready = options.ready || {items: [], pending: [0]};
            deliver(ready, baseUrl + "/bigpipe/request/" + requestId);
            started = true;
          }
          return self;
        },
        subscribe: function (channelId, subscriber) {
          if (subscribers[channelId]) {
            throw new Error("Channel '" + channelId + "' already has a subscriber");
          }
          if (subscriber) {
            subscribers[channelId] = subscriber;
            each(buffers[channelId], function (i, event) {
              publish(event);
            });
            delete buffers[channelId];
            if (closed) close(channelId);
          }
          return self;
        }
      };
      return self;
    }
    var main = BigPipe();
    main.subscribe("html", function (event) {
      if (!event.complete) {
        $("#" + event.contentId).removeClass("bp-loading").html(event.content);
        AP.resize();
      }
    });
    return extend(BigPipe, main);
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
        body = document.body,
        del = document.documentElement,
        scroll = "scrollHeight",
        offset = "offsetHeight",
        client = "clientHeight",
        dh = max(del[client], body[scroll], del[scroll], body[offset], del[offset]),
        h = height == null ? dh : height;
    return {w: w, h: h};
  }

  var api = {

    // inits the remote plugin on iframe content load
    init: function (options) {
      options = options || {};
      if (!isInited) {
        isDialog = window.location.toString().indexOf("dialog=1") > 0;
        var config = {};
        // init stubs for private bridge functions
        var stubs = {
          // !!! JIRA specific !!!
          getWorkflowConfiguration: {},
          setDialogButtonEnabled: {},
          isDialogButtonEnabled: {}
        };
        // add stubs for each public api
        each(api, function (method) { stubs[method] = {}; });
        rpc = new AP._Rpc(config, {remote: stubs, local: internal});
        rpc.init();
        // integrate the iframe with the host document
        if (options.margin !== false) {
          // inject an appropriate margin value
          injectMargin(options);
        }
        if (options.base === true) {
          // inject an appropriate base tag
          injectBase(options);
        }
        if (options.resize !== false) {
          var rate = options.resize;
          rate = rate === "auto" ? 125 : +rate;
          // force rate to an acceptable minimum if it's a number
          if (rate >= 0 && rate < 60) rate = 60;
          if (!isDialog && rate > 0) {
            // auto-resize when size changes are detected
            bind(window, "load", function () {
              var last;
              setInterval(function () {
                var curr = size();
                if (!last || last.w !== curr.w || last.h !== curr.h) {
                  AP.resize(curr.w, curr.h);
                  last = curr;
                }
              }, rate);
            });
          }
          else {
            // resize the parent iframe for the size of this document on load
            bind(window, "load", function () { AP.resize(); });
          }
        }
        if (isDialog) {
          // expose the dialog sub-api if appropriate
          AP.Dialog = makeDialog();
        }
        // !!! JIRA specific !!!
        AP.WorkflowConfiguration = makeWorkflowConfiguration();
        isInited = true;
      }
      else {
        log("Manual call to init is a deprecated no-op; use 'data-options' attribute on script to set options");
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

    // get current timezone - if user is logged in then this will retrieve user's timezone
    // the default (application/server) timezone will be used for unauthorized user
    //
    // @param callback  function (user) {...}
    getTimeZone: function (callback) {
      rpc.getTimeZone(callback);
    },

    // fire an analytics event
    //
    // @param id  the event id.  Will be prepended with the prefix "p3.iframe."
    // @param props the event properties
    fireEvent: function (id, props) {
      rpc.fireEvent(id, props);
    },

    // !!! JIRA specific !!!
    // get a workflow configuration object
    //
    // @param callback function (workflow) {...}
    getWorkflowConfiguration : function (callback) {
      var uuid = decodeURI(RegExp('remoteWorkflowPostFunctionUUID=([0-9a-z\-]+)').exec(document.location)[1]);
      rpc.getWorkflowConfiguration(uuid, callback);
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
    resize: debounce(function (width, height) {
      var dim = size(width, height);
      rpc.resize(dim.w, dim.h);
    }, 50),

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
      function fail(args) {
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
    },
    // !!! JIRA specific !!!
    setWorkflowConfigurationMessage: function () {
      return AP.WorkflowConfiguration.trigger();
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
  // !!! JIRA specific !!!
  function makeWorkflowConfiguration() {
    var workflowListener,
        validationListener;
    return {
      onSaveValidation: function (listener) {
        validationListener = listener
      },
      onSave: function (listener) {
        workflowListener = listener
      },
      trigger : function () {
        if (validationListener.call()) {
          var uuidValue = decodeURI(RegExp('remoteWorkflowPostFunctionUUID=([0-9a-z\-]+)').exec(document.location)[1]);
          return {valid : true, uuid: uuidValue, value : "" + workflowListener.call()};
        }
        else {
          return {valid : false}
        }
      }
    };
  }

  // reveal the api on the RA window
  extend(AP, api);

  // initialize
  var options,
      $script = $("script[src*='/remotable-plugins/all']");
  if ($script && /\/remotable-plugins\/all(-debug)?\.js($|\?)/.test($script.attr("src"))) {
    // normal iframe
    options = {};
    var optStr = $script.attr("data-options");
    if (optStr) {
      each(optStr.split(";"), function (i, nvpair) {
        nvpair = trim(nvpair);
        if (nvpair) {
          var nv = nvpair.split(":"), k = trim(nv[0]), v = trim(nv[1]);
          if (k && v != null) options[k] = v === "true" || v === "false" ? v === "true" : v;
        }
      });
    }
  }

  AP.init(options);

})(this, document);
