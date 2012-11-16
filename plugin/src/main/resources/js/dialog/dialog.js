(function(global, AJS, $) {

  global.RemotablePlugins = global.RemotablePlugins || {};

  var enc = encodeURIComponent;

  var $global = $(global);

  var idSeq = 0;

  /**
   * Constructs a new AUI dialog wrapper for a Remotable Plugin. The dialog has a single content panel containing a single
   * iframe. The iframe's content is retrieved from the Remotable Plugin via a redirect URl from the host Atlassian app,
   * which the request to the Remotable Plugin to be signed with outgoing OAuth credentials.
   *
   * @param contentUrl The URL (relative to the Atlassian app root) that will retrieve the content to display,
   *           eg. "/plugins/servlet/remotable-plugins/app-key/macro".
   * @param options Options to configure the behaviour and appearance of the dialog.
   */
  RemotablePlugins.makeDialog = function (contentUrl, options) {
    var $container, submitClass = "ra-dialog-submit", cancelClass = "ra-dialog-cancel";
    var defaultOptions = {
      // These options really _should_ be provided by the caller, or else the dialog is pretty pointless

      // Dialog header
      header: "Remotable Plugins Dialog Title",
      // Callback to execute when the submit button is clicked.
      submitHandler: function (dialog, result) {
        // no-op
      },
      // Callback to execute when the cancel button is clicked.
      cancelHandler: function (dialog, result) {
        // no-op
      },

      // These options may be overridden by the caller, but the defaults are OK

      headerClass: "ra-dialog-header",
      // Default width and height of the dialog
      width: "50%",
      height: "50%",
      // Close the dialog if it loses focus
      closeOnOutsideClick: true,
      // Display text for the dialog buttons
      submitText: "Submit",
      cancelText: "Cancel",

      // Escape key listener
      keypressListener: function (e) {
        if (e.keyCode === 27) {
          dialog.remove();
        }
      }
    };

    var dialogId = options.id || "ra-dialog-" + (idSeq += 1);
    var mergedOptions = $.extend({id: dialogId}, defaultOptions, options);
    mergedOptions.width = parseDimension(mergedOptions.width, $global.width());
    mergedOptions.height = parseDimension(mergedOptions.height, $global.height());

    var dialog = new AJS.Dialog(mergedOptions);
    dialog.addHeader(mergedOptions.header, mergedOptions.headerClass);
    dialog.addButton(mergedOptions.submitText, function(dialog) {
      // Disable all the buttons
      var btns = disableButtons([submitClass, cancelClass]);
      $container.trigger("ra.dialog.submit", function(result) {
        if (result.result || result) {
          dialog.remove();
          $container.trigger("ra.iframe.destroy");
          mergedOptions.submitHandler(dialog, result);
        }
        else {
          btns.enable();
        }
      });
    }, submitClass);
    dialog.addCancel(mergedOptions.cancelText, function(dialog, page) {
      // Disable buttons
      var btns = disableButtons([submitClass, cancelClass]);
      $container.trigger("ra.dialog.cancel", function(result) {
        if (result.result || result) {
          dialog.remove();
          $container.trigger("ra.iframe.destroy");
          mergedOptions.cancelHandler(dialog, result);
        }
        else {
          btns.enable();
        }
      });
    });

    var placeholderContent = "<div class='ra-servlet-placeholder'></div>";
    dialog.addPanel(null, placeholderContent, "ra-dialog-content");
    var $dialog = $("#" + dialogId);
    $container = $dialog.find(".ra-servlet-placeholder");

    return {
      id: dialogId,
      show: function() {
        dialog.show();
        var $panelBody = $dialog.find(".ra-dialog-content");
        contentUrl += (contentUrl.indexOf("?") > 0 ? "&" : "?") + "dialog=1";
        contentUrl = setDimension(contentUrl, "width", $panelBody.width());
        contentUrl = setDimension(contentUrl, "height", $panelBody.height());
        var timeout = setTimeout(function () {
          $container
            .append("<div class='ra-dialog-loading hidden'>&nbsp;</div>")
            .find(".ra-dialog-loading").height($panelBody.height()).fadeIn();
        }, 1000);

        $.ajax(contentUrl, {
          dataType: "html",
          success: function(data) {
            if (timeout) clearTimeout(timeout);
            $container.html(data);
          },
          error: function(xhr, status, ex) {
            if (timeout) clearTimeout(timeout);
            var title = "Unable to load plugin content.  Please try again later.";
            $container.html("<div class='aui-message error' style='margin: 10px'></div>");
            $container.find(".error").append("<p class='title'>" + title + "</p>");
            var msg = status + (ex ? ": " + ex.toString() : "");
            $container.find(".error").append(msg);
            AJS.log(msg);
          }
        });
      }
    };
  };

  function disableButtons(ids) {
    var btns = [];
    $.each(ids, function() {
      btns.push($('.' + this));
    });
    $.each(btns, function() {
      this.attr('disabled', 'disabled');
    });
    return {
      "enable" : function() {
        $.each(btns, function() {
          this.removeAttr('disabled');
        })
      }
    };
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

})(this, AJS, AJS.$);
