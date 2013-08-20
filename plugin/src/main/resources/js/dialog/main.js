_AP.define("dialog", ["_dollar"], function($) {

  // TODO: this should be somewhere (or on host side, can we assume _)
  function filter(from, properties) {
    from = from || {};
    var to = {};
    for (var i = 0; i < properties.length; ++i) {
      var property = properties[i];
      if (from.hasOwnProperty(property)) {
        to[property] = from[property];
      }
    }
    return to;
  }

  function PopupApi(popup) {
    $(document).on("hideLayer", function(e, type, data) {
      if ("popup" === type && data === popup) {
        // We always show the dialog when it's created, so we need to remove() when it's hidden
        popup.remove();
      }
    });
  }

  function createDialog(pluginKey, options) {
    var dialogOptions,
      popup,
      $nexus,
      contentUrl = AJS.contextPath() + "/plugins/servlet/render-signed-iframe";

    $.ajax(contentUrl, {
      dataType: "html",
      data: {
        "dialog": true,
        "plugin-key": pluginKey,
        "remote-url": options.url,
        "width": "100%",
        "height": "100%"
      },
      success: function(data) {
        $nexus.html(data);
      },
      error: function(xhr, status, ex) {
        var title = "Unable to load plugin content.  Please try again later.";
        $nexus.html("<div class='aui-message error' style='margin: 10px'></div>");
        $nexus.find(".error").append("<p class='title'>" + title + "</p>");
        var msg = status + (ex ? ": " + ex.toString() : "");
        $nexus.find(".error").append(msg);
        AJS.log(msg);
      }
    });

    dialogOptions = filter(options, ["width", "height", "id"]);
    popup = new AJS.popup(dialogOptions);
    $nexus = $("<div class='ap-servlet-placeholder'></div>").appendTo(popup.element);
    popup.show();
    return new PopupApi(popup);
  }

  function closeDialog() {
    AJS.popup.current && AJS.popup.current.hide();
  }

  return {
    create: createDialog,
    close: closeDialog
  };
});