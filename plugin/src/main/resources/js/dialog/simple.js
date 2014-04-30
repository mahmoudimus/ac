_AP.define("dialog/simple", ["_dollar", "_uri", "host/_status_helper"], function($, uri, statusHelper) {

  var enc = encodeURIComponent;

  var $global = $(window);

  var idSeq = 0;

  /**
   * Constructs a new AUI dialog wrapper for a Remotable Plugin. The dialog has a single content panel containing a single
   * iframe. The iframe's content is retrieved from the Remotable Plugin via a redirect URl from the host Atlassian app,
   * which the request to the Remotable Plugin to be signed with outgoing OAuth credentials.
   *
   * @param {String} contentUrl The URL (relative to the Atlassian app root) that will retrieve the content to display,
   *           eg. "/plugins/servlet/ac/app-key/macro".
   * @param {Object} options Options to configure the behaviour and appearance of the dialog.
   * @param {String} [options.header="Remotable Plugins Dialog Title"]  Dialog header.
   * @param {String} [options.headerClass="ap-dialog-header"] CSS class to apply to dialog header.
   * @param {String|Number} [options.width="50%"] width of the dialog, expressed as either absolute pixels (eg 800) or percent (eg 50%)
   * @param {String|Number} [options.height="50%"] height of the dialog, expressed as either absolute pixels (eg 600) or percent (eg 50%)
   * @param {String} [options.id] ID attribute to assign to the dialog. Default to "ap-dialog-n" where n is an autoincrementing id.
   */
  return function (contentUrl, options) {
    var $nexus;

    var defaultOptions = {
      // These options really _should_ be provided by the caller, or else the dialog is pretty pointless

      // Dialog header
      header: "Remotable Plugins Dialog Title",

      // These options may be overridden by the caller, but the defaults are OK
      headerClass: "ap-dialog-header",
      // Default width and height of the dialog
      width: "50%",
      height: "50%"
    };

    var dialogId = options.id || "ap-dialog-" + (idSeq += 1);
    var mergedOptions = $.extend({id: dialogId}, defaultOptions, options);
    mergedOptions.width = parseDimension(mergedOptions.width, $global.width());
    mergedOptions.height = parseDimension(mergedOptions.height, $global.height());

    var dialog = new AJS.Dialog(mergedOptions.width, mergedOptions.height, mergedOptions.id);
    dialog.addHeader(mergedOptions.header, mergedOptions.headerClass);

    var hasClosed = false;
    function closeDialog() {
      if (hasClosed) return;
      $nexus
        .trigger("ra.iframe.destroy")
        .removeData("ra.dialog.buttons")
        .unbind();
      dialog.remove();
      hasClosed = true;
    }

    // the dialog automatically closes on ESC. but we also want to do our clean up
    $(document).keydown(function(e){ if (e.keyCode === 27) { closeDialog(); }});

    var placeholderContent = "<div class='ap-servlet-placeholder'></div>";
    dialog.addPanel(null, placeholderContent, "ap-dialog-content");
    var $dialog = $("#" + dialogId);
    $nexus = $dialog.find(".ap-servlet-placeholder");
    var submitButtonText = mergedOptions.submitText || "Submit";

    function displayDialogContent(container, contentUrl){
            var contentUrlObj = new uri.init(contentUrl);
            dialogOptions = mergedOptions;
            dialogOptions.w = dialogOptions.w || dialogOptions.width;
            dialogOptions.h = dialogOptions.h || dialogOptions.height;

            if (!dialogOptions.ns) {
                dialogOptions.ns = contentUrlObj.getQueryParamValue('xdm_c').replace('channel-', '');
            }
            dialogOptions.container = dialogOptions.ns;
            dialogOptions.src = contentUrl;
            dialogOptions.dlg = true;
            if(!container.find('iframe').length){
                container.attr('id', 'ap-' + dialogOptions.ns);
                container.append('<div id="embedded-' + dialogOptions.ns + '" />');
                container.append(statusHelper.createStatusMessages());
                _AP.create(dialogOptions);

            }
    }

    return {
      id: dialogId,
      show: function() {
        dialog.show();

        var $panelBody = $dialog.find(".ap-dialog-content"),
        contentUrlObj = new uri.init(contentUrl);

        // for legacy xml descriptors we add the dialog flags when we send to the host for signing
        if(!contentUrlObj.scheme()){
            contentUrlObj.replaceQueryParam("dialog", "1").replaceQueryParam("simpleDialog", "1");
        }

        if(!contentUrlObj.getQueryParamValue("dialog") || !contentUrlObj.getQueryParamValue("simpleDialog")){
          throw "Missing dialog or simpleDialog url parameters";
        }

        function enableButtons() {
          buttons.setEnabled(true);
        }

        var buttons = makeButtons(dialog, [{
          name: "submit",
          displayName: submitButtonText,
          type: "Button",
          actions: {
            done: closeDialog
          }
        }, {
          name: "cancel",
          displayName: "Cancel",
          type: "Link",
          actions: {
            done: closeDialog,
            fail: enableButtons
          },
          noDisable: true
        }]);

        var iframeCreated = false;
        buttons.getButton("cancel").click(function () {
          if (!iframeCreated) {
            // default cancel handler should only run before the iframe is created and takes over
            closeDialog();
          }
        });

        $nexus
          .data("ra.dialog.buttons", buttons)
          .bind("ra.dialog.close", closeDialog)
          .bind("ra.iframe.create", function () { iframeCreated = true; })
          .bind("ra.iframe.init", enableButtons);
        // @todo should we instead start with all but cancel set to hidden, showing when iframe is inited?
        buttons.setEnabled(false);

        //check for json descriptor add-ons
        var scheme =contentUrlObj.scheme();
        if(scheme){
          var cont = $('<div />').appendTo('.ap-servlet-placeholder');
          displayDialogContent(cont, contentUrlObj.toString());
          return;
        }

        var timeout = setTimeout(function () {
          $nexus
            .append("<div class='ap-dialog-loading hidden'>&nbsp;</div>")
            .find(".ap-dialog-loading").height($panelBody.height()).fadeIn();
        }, 500);

        function preventTimeout() {
          if (timeout) {
            clearTimeout(timeout);
            timeout = null;
          }
        }

        // support XML descriptors
        $.ajax(contentUrlObj.toString(), {
          dataType: "html",
          success: function(data) {
            preventTimeout();
            $nexus.html(data);
          },
          error: function(xhr, status, ex) {
            preventTimeout();
            var title = "Unable to load plugin content.  Please try again later.";
            $nexus.html("<div class='aui-message error' style='margin: 10px'></div>");
            $nexus.find(".error").append("<p class='title'>" + title + "</p>");
            var msg = status + (ex ? ": " + ex.toString() : "");
            $nexus.find(".error").append(msg);
            AJS.log(msg);
          }
        });
      },
      close: closeDialog
    };
  };

  function makeButtons(dialog, specs) {
    var buttons = {},
        controls;
    $.each(specs, function () {
      var $dialog = $(dialog.popup.element),
          spec = this,
          className = "ap-dialog-" + spec.name,
          disabledAttr = "disabled",
          disabledClass = "ap-link-disabled",
          isEnabled = true;
      function dispatch(result) {
        var name = result ? "done" : "fail";
        spec.actions && spec.actions[name] && spec.actions[name]();
      }
      function handler() {
        // ignore clicks on disabled links
        if (buttons[spec.name].$el().hasClass(disabledClass)) return;
        $dialog.find("." + className).trigger("ra.dialog.click", dispatch);
      }
      dialog["add" + spec.type](spec.displayName, handler, className);
      buttons[spec.name] = {
        $el: function () { return $dialog.find("." + className); },
        isEnabled: function () { return isEnabled; },
        setEnabled: function (enabled) {
          if (!spec.noDisable) {
            var $button = this.$el();
            if (enabled) {
              if (spec.type === "Button") {
                $button.removeAttr(disabledAttr);
              }
              else {
                $button.addClass(disabledClass);
              }
            }
            else {
              if (spec.type === "Button") {
                $button.attr(disabledAttr, true);
              }
              else {
                $button.removeClass(disabledClass);
              }
            }
          }
        },
        click: function (listener) {
          if (listener) {
            this.$el().bind("ra.dialog.click", listener);
          }
          else {
            dispatch(true);
          }
        }
      };
    });
    controls = {
      each: function (it) {
        $.each(buttons, it);
      },
      setEnabled: function (enabled) {
        this.each(function () {
          this.setEnabled(enabled);
        });
      },
      getButton: function (name) {
        return buttons[name];
      }
    };
    return controls;
  }

  function parseDimension(value, viewport) {
    if (typeof value === "string") {
      var percent = value.indexOf("%") === value.length - 1;
      value = parseInt(value, 10);
      if (percent) value = value / 100 * viewport;
    }
    return value;
  }

  function setDimension(url, name, value) {
    name = enc(name);
    if (url.indexOf(name + "=")) {
      url = url.replace(new RegExp(name + "=[^&]+"), function () {
        return name + "=" + enc(value);
      });
    }
    else {
      url += "&" + name + "=" + enc(value);
    }
    return url;
  }

});


/**
 * Legacy namespace
 * @deprecated
 */
if (!_AP.makeDialog) {
  _AP.require(["dialog/simple"], function(main) {
    _AP.makeDialog = main;
  });
}
