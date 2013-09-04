_AP.define("dialog", ["_dollar"], function($) {

    // Should be ok to reference the nexus at this level since there should only be one dialog open at a time
  var $nexus;

  var $dialog; // active dialog element

  function createDialog(pluginKey, options) {
    var contentUrl = AJS.contextPath() + "/plugins/servlet/render-signed-iframe";

    if ($nexus) throw new Error("Only one dialog can be open at once");

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

    $dialog = createDialogElement(options.id, options.titleId, options.size).appendTo(AJS.$("body"));
    if (options.width || options.height) {
      _AP.AJS.layer($dialog).changeSize(options.width, options.height);
    }

    var dialog = _AP.AJS.dialog2($dialog);
    $nexus = $("<div class='ap-servlet-placeholder ap-dialog-container'></div>").appendTo($dialog);
    dialog.on("hide", function() {
      // We always show the dialog when it's created, so we need to remove() when it's hidden
      dialog.remove();
    });
    dialog.show();
    return dialog;
  }

  function createDialogElement(id, titleId, size) {
    // TODO: copied from AUI dialog2 soy. Should make it use that when it's in products.
    var $el = AJS.$("<section></section>")
      .addClass("ap-aui-layer").addClass("ap-aui-layer-hidden").addClass("ap-aui-layer-modal")
      .addClass("ap-aui-dialog2").addClass("ap-aui-dialog2-" + (size || "medium"))
      .attr("role", "dialog")
      .attr("data-aui-blanketed", "true")
      .attr("data-aui-focus-selector", ".aui-dialog2-content :input:visible:enabled");

    if (id) {
      $el.attr("id", id);
    }
    if (titleId) {
      $el.attr("aria-labelledby", titleId);
    }
    return $el;
  }

  function closeDialog() {
    if ($nexus) {
      // Signal the XdmRpc for the dialog's iframe to clean up
      $nexus.trigger("ra.iframe.destroy");
      // Clear the nexus handle to allow subsequent dialogs to open
      $nexus = null;
    }
    _AP.AJS.dialog2($dialog).hide();
  }

  return {
    create: createDialog,
    close: closeDialog
  };
});
