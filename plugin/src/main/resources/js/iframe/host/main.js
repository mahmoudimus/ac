/**
 * Entry point for xdm messages on the host product side.
 */
_AP.define("host/main", ["_xdm-rpc"], function (XdmRpc) {

  var $ = AJS.$,
      xhrProperties = ["status", "statusText", "responseText"],
      xhrHeaders = ["Content-Type"],
      events = (AJS.EventQueue = AJS.EventQueue || []),
      defer = window.requestAnimationFrame || function (f) {setTimeout(f,10)};

  function contentDiv(ns) {
    return $("#embedded-" + ns);
  }

  function create(options) {

    var ns = options.ns,
        homeId = "ap-" + ns,
        $home = $("#" + homeId),
        $content = contentDiv(ns),
        contentId = $content.attr("id"),
        channelId = "channel-" + ns,
        initWidth = options.w || "100%",
        initHeight = options.h || "0",
        start = new Date().getTime(),
        isDialog = !!options.dlg,
        isInited;

    function publish(name, props) {
      props = $.extend(props || {}, {moduleKey: ns});
      events.push({name: name, properties: props});
    }

    function showStatus(name) {
      $home.find(".ap-status").addClass("hidden");
    }

    var timeout = setTimeout(function () {
      timeout = null;
      showStatus("load-timeout");
      var $timeout = $home.find(".ap-load-timeout");
      $timeout.find("a.ap-btn-cancel").click(function () {
        showStatus("load-error");
        $nexus.trigger(isDialog ? "ra.dialog.close" : "ra.iframe.destroy");
      });
      layoutIfNeeded();
      publish("plugin.iframetimedout", {elapsed: new Date().getTime() - start});
    }, 20000);

    function preventTimeout() {
      if (timeout) {
        clearTimeout(timeout);
        timeout = null;
      }
    }

    function getDialogButtons() {
      return $nexus.data("ra.dialog.buttons");
    }

    function getDialogButton(name) {
      return $nexus.data("ra.dialog.buttons").getButton(name);
    }

    var rpc = new XdmRpc({
      remote: options.src,
      container: contentId,
      channel: channelId,
      props: {width: initWidth, height: initHeight}
    }, {
      remote: [
        "dialogMessage",
        // !!! JIRA specific !!!
        "setWorkflowConfigurationMessage"
      ],
      local: {
        init: function () {
          if (!isInited) {
            isInited = true;
            preventTimeout();
            $content.addClass("iframe-init");
            var elapsed = new Date().getTime() - start;
            showStatus("loaded");
            layoutIfNeeded();
            $nexus.trigger("ra.iframe.init");
            publish("plugin.iframeinited", {elapsed: elapsed});
          }
        },
        resize: debounce(function (width, height) {
          // debounce resizes to avoid excessive page reflow
          if (!isDialog) {
            // dialog content plugins do not honor resize requests, since their content size is fixed
            $("iframe", $content).css({width: width, height: height});
          }
        }),
        getLocation: function () {
          return window.location.href;
        },
        getUser: function () {
          // JIRA 5.0, Confluence 4.3(?)
          var meta = AJS.Meta,
              fullName = meta ? meta.get("remote-user-fullname") : null;
          if (!fullName) {
            // JIRA 4.4, Confluence 4.1, Refapp 2.15.0
            fullName = $("a#header-details-user-fullname, .user.ajs-menu-title, a#user").text();
          }
          if (!fullName) {
            // JIRA 6, Confluence 5
            fullName = $("a#user-menu-link").attr("title");
          }
          return {fullName: fullName, id: options.uid, key: options.ukey};
        },
        getTimeZone: function () {
          return options.data.timeZone;
        },
        showMessage: function (id, title, body) {
          // init message bar if necessary
          if ($("#aui-message-bar").length === 0) {
            // @todo adding #aui-message-bar only works for the view-issue page for now
            $.html("div").attr("id", "aui-message-bar").prependTo("#details-module");
          }
          $(".aui-message#" + id).remove();
          AJS.messages.info({
            title: title,
            body: "<p>" + $("<div/>").text(body).html() + "<p>",
            id: id,
            closeable: false
          });
        },
        clearMessage: function (id) {
          $(".aui-message#" + id).remove();
        },
        setDialogButtonEnabled: function (name, enabled) {
          var button = getDialogButton(name);
          if (button) button.setEnabled(enabled);
        },
        isDialogButtonEnabled: function (name, callback) {
          var button = getDialogButton(name);
          callback(button ? button.isEnabled() : void 0);
        },
        request: function (args, success, error) {
          // add the context path to the request url
          var url = options.cp + args.url;
          // reduce the xhr object to the just bits we can/want to expose over the bridge
          function toJSON(xhr) {
            var json = {headers: {}};
            // only copy key properties and headers for transport across the bridge
            $.each(xhrProperties, function (i, v) { json[v] = xhr[v]; });
            // only copy key response headers for transport across the bridge
            $.each(xhrHeaders, function (i, v) { json.headers[v] = xhr.getResponseHeader(v); });
            return json;
          }
          function done(data, textStatus, xhr) {
            success([data, textStatus, toJSON(xhr)]);
          }
          function fail(xhr, textStatus, errorThrown) {
            error([toJSON(xhr), textStatus, errorThrown]);
          }
          var headers = {};
          $.each(args.headers || {}, function (k, v) { headers[k.toLowerCase()] = v; });
          // execute the request with our restricted set of inputs
          $.ajax({
            url: url,
            type: args.type || "GET",
            data: args.data,
            dataType: "text", // prevent jquery from parsing the response body
            contentType: args.contentType,
            headers: {
              // */* will undo the effect on the accept header of having set dataType to "text"
              "Accept": headers.accept || "*/*",
              // send the client key header to force scope checks
              "AP-Client-Key": options.key
            }
          }).then(done, fail);
        },
        // !!! JIRA specific !!!
        getWorkflowConfiguration: function (uuid, callback) {
          callback($("#remoteWorkflowPostFunctionConfiguration-"+uuid).val());
        },
        // !!! Confluence specific !!!
        saveMacro: function(updatedParams) {
          _AP.require("confluence/macro/editor", function(editor) {
              editor.saveMacro(updatedParams);
          });
        },
        closeMacroEditor: function () {
            _AP.require("confluence/macro/editor", function (editor) {
                editor.close();
            })
        }
      }
    });

    var $nexus = $content.parents(".ap-servlet-placeholder"),
        $iframe = $("iframe", $content);

    $iframe.data("ap-rpc", rpc);

    function layoutIfNeeded() {
      var $stats = $(".ap-stats", $home);
      if (isDialog) {
        var panelHeight = $nexus.parent().height();
        $iframe.parents(".ap-servlet-placeholder, .ap-container").height(panelHeight);
        var containerHeight = $iframe.parents(".ap-container").height(),
            iframeHeight = containerHeight - $stats.outerHeight(true);
        $iframe.height(iframeHeight);
        $content.height(iframeHeight);
      }
      $stats.removeClass("hidden");
    }

    layoutIfNeeded();

    // a simplified version of underscore's debounce
    function debounce(fn, wait) {
      var timeout;
      return function() {
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

    // wireup dialog buttons if appropriate
    var dialogButtons = getDialogButtons();
    if (dialogButtons) {
      dialogButtons.each(function (name, button) {
        button.click(function (e, callback) {
          if (isInited) {
            rpc.dialogMessage(name, callback);
          }
          else {
            callback(true);
          }
        });
      });
    }

    // !!! JIRA specific !!!
    var done = false;
    $(document).delegate("#add_submit, #update_submit", "click", function (e) {
      if (!done) {
        e.preventDefault();
        rpc.setWorkflowConfigurationMessage(function (either) {
          if (either.valid) {
            $("#remoteWorkflowPostFunctionConfiguration-" + either.uuid).val(either.value);
            done = true;
            $(e.target).click();
          }
        });
      }
    });
    // !!! end JIRA !!!

    // clean up when the iframe is removed by other sceripts coordinating through the $nexus
    $nexus.bind("ra.iframe.destroy", function () {
      // destroy the rpc bridge and remove the iframe
      rpc.destroy();
    });

    $nexus.trigger("ra.iframe.create");
  }

  return function (options) {
    function doCreate() {
      // make sure the content div is empty
      contentDiv(options.ns).find("iframe").each(function (_, iframe) {
        var rpc = $(iframe).data("ap-rpc");
        if (rpc) rpc.destroy();
      });
      // create the new iframe
      create(options);
    }
    if ($.isReady) {
      // if the dom is ready then this is being run during an ajax update;
      // in that case, defer creation until the next event loop tick to ensure
      // that updates to the desired container node's parents have completed
      defer(doCreate);
    }
    else {
      // if the document hasn't yet loaded, create immediately
      doCreate();
    }
  };

});

// Legacy global namespace
// TODO: should be able to express this as _AP.create = _AP.require("host/main"). Requires changes in _amd.js
if (!_AP.create) {
  _AP.require(["host/main"], function(main) {
    _AP.create = main;
  });
}
