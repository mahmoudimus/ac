(function (global, AJS) {

  var $ = AJS.$,
      RA = global.RemotablePlugins = global.RemotablePlugins || {},
      xhrProperties = ["status", "statusText", "responseText"],
      xhrHeaders = ["Content-Type"],
      events = (AJS.EventQueue = AJS.EventQueue || []);

  RA.create = RA.create || function (options) {

    var ns = options.ns,
        $home = $("#ra-" + ns),
        contentId = "embedded-" + ns,
        channelId = "channel-" + ns,
        $content = $("#" + contentId),
        initWidth = options.width || "100%",
        initHeight = options.height || "0",
        start = new Date().getTime(),
        isDialog = !!options.dialog,
        inited;

    function track(name, props) {
      props = $.extend(props || {}, {moduleKey: ns});
      events.push({name: name, properties: props});
    }

    var timeout = setTimeout(function () {
      $home.find(".ra-message").addClass("hidden");
      $home.find(".ra-timedout").removeClass("hidden");
      layoutIfNeeded();
      var elapsed = new Date().getTime() - start;
      track("plugin.iframetimedout", {elapsed: elapsed});
    }, 20000);

    var rpc = new easyXDM.Rpc({
      remote: options.src,
      container: contentId,
      channel: channelId,
      protocol: "1", // force to postMessage
      props: {width: initWidth, height: initHeight}
    }, {
      remote: {
        dialogMessage: {}
      },
      local: {
        init: function () {
          if (!inited) {
            inited = true;
            if (timeout) {
              clearTimeout(timeout);
            }
            $content.addClass("iframe-init");
            $home.find(".ra-message").addClass("hidden");
            var elapsed = new Date().getTime() - start;
            $home.find(".ra-elapsed").text(elapsed);
            $home.find(".ra-loaded").removeClass("hidden");
            layoutIfNeeded();
            track("plugin.iframeinited", {elapsed: elapsed});
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
          return {fullName: fullName, id: options.userId};
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
              "RA-App-Key": options.appKey
            }
          }).then(done, fail);
        }
      }
    });

    function layoutIfNeeded() {
      var $stats = $(".ra-stats", $home);
      if (isDialog) {
        var $placeholder = $content.parents(".ra-servlet-placeholder"),
            $iframe = $("iframe", $content),
            panelHeight = $placeholder.parent().height();
        $iframe.parents(".ra-servlet-placeholder, .ra-container").height(panelHeight);
        var containerHeight = $iframe.parents(".ra-container").height(),
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

    // connects rpc functions to local event channels
    (function () {
      $content.parents(".ra-servlet-placeholder")
        .bind("ra.dialog.submit", function (e, callback) {
          rpc.dialogMessage("submit", callback);
        })
        .bind("ra.dialog.cancel", function (e, callback) {
          rpc.dialogMessage("cancel", callback);
        })
        .bind("ra.iframe.destroy", function () {
          rpc.destroy();
        });
    })();

  }

}(this, AJS));
