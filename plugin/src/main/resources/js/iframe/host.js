(function (global, AJS) {

  var $ = AJS.$,
      AP = global._AP = global._AP || {},
      xhrProperties = ["status", "statusText", "responseText"],
      xhrHeaders = ["Content-Type"],
      events = (AJS.EventQueue = AJS.EventQueue || []);

  AP.create = AP.create || function (options) {

    var ns = options.ns,
        $home = $("#ap-" + ns),
        contentId = "embedded-" + ns,
        channelId = "channel-" + ns,
        $content = $("#" + contentId),
        initWidth = options.width || "100%",
        initHeight = options.height || "0",
        start = new Date().getTime(),
        isDialog = !!options.dialog,
        isInited;

    function publish(name, props) {
      props = $.extend(props || {}, {moduleKey: ns});
      events.push({name: name, properties: props});
    }

    function showStatus(name) {
      $home.find(".ap-status").addClass("hidden");
      $home.find(".ap-" + name).removeClass("hidden");
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

    var rpc = new easyXDM.Rpc({
      remote: options.src,
      container: contentId,
      channel: channelId,
      protocol: "1", // force to postMessage
      props: {width: initWidth, height: initHeight}
    }, {
      remote: {
        dialogMessage: {},
        // !!! JIRA specific !!!
        setWorkflowConfigurationMessage: {}
      },
      local: {
        init: function () {
          if (!isInited) {
            isInited = true;
            preventTimeout();
            $content.addClass("iframe-init");
            var elapsed = new Date().getTime() - start;
            //$home.find(".ap-elapsed").text(elapsed);
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
        fireEvent: function(id, props) {
            publish("p3.iframe." + id, props);
        },
        getLocation: function () {
          return global.location.href;
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
          return {fullName: fullName, id: options.userId};
        },
        getTimeZone: function () {
          return options.data.timeZone;
        },
        // !!! JIRA specific !!!
        getWorkflowConfiguration: function (uuid, callback) {
          callback($("#remoteWorkflowPostFunctionConfiguration-"+uuid).val());
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
          var url = options.contextPath + args.url;
          // reduce the xhr object to the just bits we can/want to expose over the bridge
          function toJSON(xhr) {
            var json = {headers: {}};
            // only copy key properties and headers for transport across the bridge
            $.each(xhrProperties, function (i, v) { json[v] = xhr[v]; });
            // only copy key response headers for transport across the bridge
            $.each(xhrHeaders, function (i, v) { json.headers[v] = xhr.getResponseHeader(v); });
            return json;
          }
          function done(data, textStatus, xhr) { success([data, textStatus, toJSON(xhr)]); }
          function fail(xhr, textStatus, errorThrown) { error([toJSON(xhr), textStatus, errorThrown]); }
          // execute the request with our restricted set of inputs
          $.ajax({
            url: url,
            type: args.type || "GET",
            data: args.data,
            dataType: "text", // prevent jquery from parsing the response body
            contentType: args.contentType,
            headers: {
              // undo the effect on the accept header of having set dataType to "text"
              "Accept": "*/*",
              // send the app key header to force scope checks
              "AP-App-Key": options.appKey
            }
          }).then(done, fail);
        }
      }
    });

    var $nexus = $content.parents(".ap-servlet-placeholder"),
        $iframe = $("iframe", $content);

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
    $(document).delegate("#add_submit", "click", function(e) {
      if (!done) {
        e.preventDefault();
        rpc.setWorkflowConfigurationMessage(function(either) {
          if (either.valid) {
            $("#remoteWorkflowPostFunctionConfiguration-"+either.uuid).val(either.value);
            done = true;
            $("#add_submit").click();
          }
        });
      }
    });
    $(document).delegate("#update_submit", "click", function(e) {
      if (!done) {
        e.preventDefault();
        rpc.setWorkflowConfigurationMessage(function(either) {
          if (either.valid) {
            $("#remoteWorkflowPostFunctionConfiguration-"+either.uuid).val(either.value);
            done = true;
            $("#update_submit").click();
          }
        });
      }
    });

    // clean up when the iframe is removed
    $nexus.bind("ra.iframe.destroy", function () {
      // destroy the rpc bridge and remove the iframe
      rpc.destroy();
    });

    $nexus.trigger("ra.iframe.create");

  }

}(this, AJS));
