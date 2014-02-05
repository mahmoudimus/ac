_AP.define("inline-dialog/simple", ["_dollar", "host/_status_helper"], function($, statusHelper) {
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
            if (!options.cp) {
                options.cp = AJS.contextPath();
            }
            options.container = options.ns;
            options.src = options.url = options.url || contentUrl;
            content.data('inlineDialog', $inlineDialog);

            if(!content.find('iframe').length){
                content.attr('id', 'ap-' + options.ns);
                content.append('<div id="embedded-' + options.ns + '" />');
                content.append(statusHelper.createStatusMessages());
                _AP.create(options);
            }
            showPopup();
            return false;
        };

        //Create the AUI inline dialog with a unique ID.
        $inlineDialog = AJS.InlineDialog(
            options.bindTo,
            //assign unique id to inline Dialog
            "ap-inline-dialog-content-" + itemId,
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
