_AP.define("inline-dialog/simple", ["_dollar", "host/_status_helper", "host/_util", 'host/content'], function($, statusHelper, util, hostContentUtilities) {
    return function (contentUrl, options) {
        var $inlineDialog;

        // Find the web-item that was clicked, we'll be needing its ID.
        if (!options.bindTo || !options.bindTo.jquery) {
            return;
        }

        var webItem = options.bindTo.hasClass("ap-inline-dialog") ? options.bindTo : options.bindTo.closest(".ap-inline-dialog");
        var itemId = webItem.attr("id");
        if (!itemId) {
            return;
        }

        var displayInlineDialog = function(content, trigger, showPopup) {

            options.w = options.w || options.width;
            options.h = options.h || options.height;
            if (!options.ns) {
                options.ns = itemId;
            }
            options.container = options.ns;
            options.src = options.url = options.url || contentUrl;
            content.data('inlineDialog', $inlineDialog);

            if(!content.find('iframe').length){

                content.attr('id', 'ap-' + options.ns);
                var containerId = 'embedded-' + options.ns;
                content.append('<div id="' + containerId + '" />');
                content.append(statusHelper.createStatusMessages());
                var container = $('#' + containerId);
                var uiParams = $.extend({dlg: 1}, options.uiParams);

                 // make an iframe inside its parent div
                hostContentUtilities.getIframeHtmlForKey(options.key, options.context, {'key':options.moduleKey}, uiParams)
                 .done(function(data) {
                        var dialogHtml = $(data);
                        dialogHtml.addClass('ap-dialog-container');
                        container.replaceWith(dialogHtml);
                        dialogHtml.find('.ap-content').addClass('iframe-init');
                    })
                 .fail(function(xhr, status, ex) {
                        var title = $("<p class='title' />").text("Unable to load add-on content. Please try again later.");
                        container.html("<div class='aui-message error ap-aui-message'></div>");
                        container.find(".error").append(title);
                        var msg = status + (ex ? ": " + ex.toString() : "");
                        container.find(".error").text(msg);
                        AJS.log(msg);
                    });
            }
            showPopup();
            return false;
        };

        var dialogElementIdentifier = "ap-inline-dialog-content-" + itemId;

        $inlineDialog = $("#inline-dialog-" + util.escapeSelector(dialogElementIdentifier));

        if($inlineDialog.length !== 0){
            $inlineDialog.remove();
        }

        //Create the AUI inline dialog with a unique ID.
        $inlineDialog = AJS.InlineDialog(
            options.bindTo,
            //assign unique id to inline Dialog
            dialogElementIdentifier,
            displayInlineDialog,
            options
        );

        return {
            id: $inlineDialog.attr('id'),
            show: function() {
                $inlineDialog.show();
            },
            hide: function() {
                $inlineDialog.hide();
            }
        };

    };

});
