_AP.define("dialog", ["_dollar", "host/content", "_ui-params"], function($, hostContentUtilities, UiParams) {

    // Should be ok to reference the nexus at this level since there should only be one dialog open at a time
  var $nexus;

  var $dialog; // active dialog element


  var uiOptions = {
    dlg: 1
  };

  // Deprecated. This passes the raw url to ContextFreeIframePageServlet, which is vulnerable to spoofing.
  // Will be removed - plugins should pass key of the <dialog-page>, NOT the url.
  // TODO: Remove this class when support for XML Descriptors goes away
  function getIframeHtmlForUrl(pluginKey, options) {
    var contentUrl = AJS.contextPath() + "/plugins/servlet/render-signed-iframe";
    return $.ajax(contentUrl, {
      dataType: "html",
      data: {
        "dialog": true,
        "ui-params": UiParams.encode(uiOptions),
        "plugin-key": pluginKey,
        "remote-url": options.url,
        "width": "100%",
        "height": "100%",
        "raw": "true"
      }
    });
  }

  function createDialog(pluginKey, productContextJson, options) {

    if ($nexus) throw new Error("Only one dialog can be open at once");

    var promise = options.url ? getIframeHtmlForUrl(pluginKey, options) : hostContentUtilities.getIframeHtmlForKey(pluginKey, productContextJson, options, uiOptions);

    promise
      .done(function(data) {
        $nexus.html(data);
      })
      .fail(function(xhr, status, ex) {
        var title = "Unable to load plugin content. Please try again later.";
        $nexus.html("<div class='aui-message error' style='margin: 10px'></div>");
        $nexus.find(".error").append("<p class='title'>" + title + "</p>");
        var msg = status + (ex ? ": " + ex.toString() : "");
        $nexus.find(".error").append(msg);
        AJS.log(msg);
      });

    $dialog = createDialogElement(options.id, options.titleId, options.size).appendTo(AJS.$("body"));
    if (options.width || options.height) {
      AJS.layer($dialog).changeSize(options.width, options.height);
    }

    var dialog = AJS.dialog2($dialog);
    $nexus = $("<div class='ap-servlet-placeholder ap-dialog-container'></div>").appendTo($dialog);
    dialog.on("hide", function() {
      // We always show the dialog when it's created, so we need to remove() when it's hidden
      dialog.remove();
    });
    dialog.show();
    AJS.dim();
    return dialog;
  }

  function createDialogElement(id, titleId, size) {
    // TODO: copied from AUI dialog2 soy. Should make it use that when it's in products.
    var $el = AJS.$("<section></section>")
      .addClass("aui-layer aui-layer-hidden aui-layer-modal")
      .addClass("aui-dialog2 aui-dialog2-" + (size || "medium"))
      .addClass("ap-aui-dialog2")
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
    if($dialog){
      $dialog.data('aui-remove-on-hide', true);
      AJS.dialog2($dialog).hide();
    } else {
        AJS.$('.aui-dialog .ap-servlet-placeholder').trigger('ra.dialog.close');
    }
    AJS.undim();
  }

  return {
    create: createDialog,
    close: closeDialog
  };
});
