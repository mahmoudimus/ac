(function (global, $) {

  var cssProperties = ["color", "fontFamily", "fontSize", "fontSizeAdjust", "fontStretch", "fontStyle", "fontVariant", "fontWeight"],
      // don't send line-height for ie, as at least ie8 reports bad values
      bodyProperties = cssProperties.concat((/*@cc_on!@*/false) ? [] : ["lineHeight"]),
      xhrProperties = ["status", "statusText", "responseText"],
      xhrHeaders = ["Content-Type"],
      RA = global.RemoteApps;

  RA.create = RA.create || function (options) {
    var ns = options.ns,
        containerId = "embedded-" + ns,
        channelId = "channel-" + ns,
        container$ = $("#" + containerId),
        initHeight = options.height || "0",
        initWidth = options.width || "100%",
        start = new Date().getTime();

    var rpc = new easyXDM.Rpc({
      remote: options.src,
      container: containerId,
      channel: channelId,
      protocol: "1", // force postMessage
      props: {height: initHeight, width: initWidth}
    }, {
      remote: {
        dialogMessage: {}
      },
      local: {
        init: function () {
          container$.addClass("iframe-init");
          $("#ra-time-" + ns).text(new Date().getTime() - start);
        },
        resize: debounce(function (width, height) {
          // debounce resizes to avoid excessive page reflow
          $("iframe", container$).css({height: height, width: width});
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
        getStylesheet: function (success) {
          var body = {};
          function capture(dest, el$, props) {
            $.each(props, function (i, k) {
              var v = el$.css(k);
              if (v != null && v !== '') {
                k = k.replace(/[A-Z]/g, function ($0) { return "-" + $0.toLowerCase(); });
                dest[k] = v;
              }
            });
          }
          capture(body, container$, bodyProperties);
          success([{selector: "body", properties: body}]);
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
    function bind() {
      var placeholder$ = container$.parents(".ra-servlet-placeholder");

      placeholder$.bind("ra.dialog.submit", function (e, callback) {
        rpc.dialogMessage("submit", callback);
      });

      placeholder$.bind("ra.dialog.cancel", function (e, callback) {
        rpc.dialogMessage("cancel", callback);
      });

      placeholder$.bind("ra.iframe.destroy", function () {
        rpc.destroy();
      });
    }

    bind();

  }

}(this, AJS.$));
