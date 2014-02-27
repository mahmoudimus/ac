_AP.define("dialog/simple-dialog", ["_dollar", "_uri", "host/_status_helper"], function($, uri, statusHelper) {

    var enc = encodeURIComponent;
    var $global = $(window);
    var idSeq = 0;

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

    /**
    * Constructs a new AUI dialog wrapper for a Remotable Plugin. The dialog has a single content panel containing a single
    * iframe. The iframe's content is retrieved from the Remotable Plugin via a redirect URl from the host Atlassian app,
    * which the request to the Remotable Plugin to be signed with outgoing OAuth credentials.
    *
    * @param {String} contentUrl The URL (relative to the Atlassian app root) that will retrieve the content to display,
    *           eg. "/plugins/servlet/atlassian-connect/app-key/macro".
    * @param {Object} options Options to configure the behaviour and appearance of the dialog.
    * @param {String} [options.header="Remotable Plugins Dialog Title"]  Dialog header.
    * @param {String} [options.headerClass="ap-dialog-header"] CSS class to apply to dialog header.
    * @param {String|Number} [options.width="50%"] width of the dialog, expressed as either absolute pixels (eg 800) or percent (eg 50%)
    * @param {String|Number} [options.height="50%"] height of the dialog, expressed as either absolute pixels (eg 600) or percent (eg 50%)
    * @param {String} [options.id] ID attribute to assign to the dialog. Default to "ap-dialog-n" where n is an autoincrementing id.
    */
    return function (contentUrl, options) {
        var $nexus;
        var hasClosed = false;

        var defaultOptions = {
            // These options really _should_ be provided by the caller, or else the dialog is pretty pointless
            width: "50%",
            height: "50%"
        };

        var dialogId = options.id || "ap-dialog-" + (idSeq += 1);
        var mergedOptions = $.extend({id: dialogId}, defaultOptions, options);
        mergedOptions.width = parseDimension(mergedOptions.width, $global.width());
        mergedOptions.height = parseDimension(mergedOptions.height, $global.height());


        function createDialogElement(options){
            // TODO: copied from AUI dialog2 soy. Should make it use that when it's in products.
            var $el = AJS.$("<section></section>")
              .addClass("aui-layer aui-layer-hidden aui-layer-modal")
              .addClass("aui-dialog2 aui-dialog2-" + (options.size || "medium"))
              .addClass("ap-aui-dialog2")
              .attr("role", "dialog")
              .attr("data-aui-blanketed", "true")
              .attr("data-aui-focus-selector", ".aui-dialog2-content :input:visible:enabled");

            if (options.id) {
              $el.attr("id", id);
            }
            if (options.titleId) {
              $el.attr("aria-labelledby", options.titleId);
            }
            return $el;
        }

        var dialogElement = this.createDialogElement(mergedOptions);
        var dialog = AJS.Dialog2(dialogElement);


        function closeDialog() {
          if (hasClosed) return;
          $nexus
            .trigger("ra.iframe.destroy")
            .unbind();
          dialog.remove();
          hasClosed = true;
        }

        // the dialog automatically closes on ESC. but we also want to do our clean up
        $(document).keydown(function(e){ if (e.keyCode === 27) { this.closeDialog(); }});

        $nexus = dialog.find(".ap-servlet-placeholder");

        return {
            id: dialogId,
            show: function() {
                dialog.show();
            },
            close: closeDialog
        };
    };

});

