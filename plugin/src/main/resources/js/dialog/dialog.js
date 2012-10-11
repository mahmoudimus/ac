(function(global, $) {

    global.RemotablePlugins = global.RemotablePlugins || {};

    var idSeq = 0;

    function disableButtons(ids) {
        var btns = [];
        $.each(ids, function() {
            btns.push($('.' + this));
        });
        $.each(btns, function() {
            this.attr('disabled', 'disabled');
        });
        return {
            'enable' : function() {
                $.each(btns, function() {
                    this.removeAttr('disabled');
                })
            }
        }
    }

    /**
     * Constructs a new AUI dialog wrapper for a Remotable Plugin. The dialog has a single content panel containing a single
     * iframe. The iframe's content is retrieved from the Remotable Plugin via a redirect URl from the host Atlassian app,
     * which the request to the Remotable Plugin to be signed with outgoing OAuth credentials.
     *
     * @param contentUrl The URL (relative to the Atlassian app root) that will retrieve the content to display,
     *                   eg. "/plugins/servlet/remotable-plugins/app-key/macro".
     * @param options Options to configure the behaviour and appearance of the dialog.
     */
    RemotablePlugins.makeDialog = function (contentUrl, options) {
        var global$ = $(global);
        var placeholderContainer$;
        var defaultOptions = {
            /**
             * These options really _should_ be provided by the caller, or else the dialog is pretty pointless.
             */
            // Dialog header
            header: "Remotable Plugins Dialog Title",
            // Callback to execute when the submit button is clicked.
            submitHandler: function (dialog, result) {
                // No-op
            },
            // Callback to execute when the cancel button is clicked.
            cancelHandler: function (dialog, result) {
                // No-op
            },

            /**
             * These options may be overridden by the caller, but the defaults are OK.
             */
            headerClass: "ra-dialog-header",
            // Default width and height of the dialog
            width: global$.width() * .5,
            height: global$.height() * .5,
            // Close the dialog if it loses focus
            closeOnOutsideClick: true,
            // Display text for the dialog buttons
            submitText: "Submit",
            cancelText: "Cancel",

            /**
             * These options shouldn't be modified by the caller at all, or things will break. I probably should move
             * these out of the options object.
             */
            submitClass: "ra-dialog-submit",
            cancelClass: "ra-dialog-cancel",
            keypressListener: function (e) {
                if (e.keyCode === 27) {
                    dialog.remove();
                }
            }
        };

        var dialogId = options.id || "ra-dialog-" + (idSeq += 1);
        var mergedOptions = $.extend({id: dialogId}, defaultOptions, options);

        var dialog = new AJS.Dialog(mergedOptions);
        dialog.addHeader(mergedOptions.header, mergedOptions.headerClass);
        dialog.addButton(mergedOptions.submitText, function(dialog) {
            // Disable all the buttons
            var btns = disableButtons([mergedOptions.submitClass, mergedOptions.cancelClass]);
            placeholderContainer$.trigger("ra.dialog.submit", function(result) {
                if (result.result || result) {
                    dialog.remove();
                    placeholderContainer$.trigger("ra.iframe.destroy");
                    mergedOptions.submitHandler(dialog, result);
                }
                else {
                    btns.enable();
                }
            });
        }, mergedOptions.submitClass);
        dialog.addCancel(mergedOptions.cancelText, function(dialog, page) {
            // Disable Buttons
            var btns = disableButtons([mergedOptions.submitClass, mergedOptions.cancelClass]);
            placeholderContainer$.trigger("ra.dialog.cancel", function(result) {
                if (result.result || result) {
                    dialog.remove();
                    placeholderContainer$.trigger("ra.iframe.destroy");
                    mergedOptions.cancelHandler(dialog, result);
                }
                else {
                    btns.enable();
                }
            });
        });
        var placeHolderContent = "<div class='ra-servlet-placeholder'>Loading...</div>";
        dialog.addPanel("Main", placeHolderContent, "ra-dialog-content");

        return {
            id: dialogId,
            show: function() {
                dialog.show();

                var panelBody = $("#" + dialogId + " .ra-dialog-content");
                contentUrl += contentUrl.indexOf("?") > 0 ? "&" : "?";
                contentUrl += "width=" + panelBody.width() + "&height=" + panelBody.height();

                $.ajax(contentUrl, {
                    dataType: "html",
                    success: function(data) {
                        placeholderContainer$ = $("#" + dialogId + " .ra-servlet-placeholder");
                        placeholderContainer$.html(data);
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        // TODO: Make this error message a bit nicer and more informative.
                        $("#" + dialogId + " .ra-servlet-placeholder").html("<p>The content could not be retrieved.</p>");
                        AJS.log(textStatus + " " + errorThrown);
                    }
                });
            }
        };
    };

})(this, AJS.$);
