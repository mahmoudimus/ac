_AP.define("dialog/dialog-factory", ["_dollar", "dialog/main", 'host/content'], function($, dialog, hostContentUtilities) {
    //might rename this, it opens a dialog by first working out the url (used for javascript opening a dialog).
    /**
    * opens a dialog by sending the add-on and module keys back to the server for signing.
    * Used by dialog-pages, confluence macros and opening from javascript.
    * @param {Object} options for passing to AP.create
    * @param {Object} dialog options (width, height, etc)
    * @param {String} productContextJson pass context back to the server
    */
    return function(options, dialogOptions, productContext) {
        var promise,
        container,
        module = {key: options.moduleKey},
        uiParams = $.extend({dlg: 1}, options.uiParams);
        dialog.create({
            id: options.id,
            ns: options.moduleKey || options.key,
            chrome: dialogOptions.chrome || options.chrome,
            header: dialogOptions.header,
            width: dialogOptions.width,
            height: dialogOptions.height,
            size: dialogOptions.size,
            submitText: dialogOptions.submitText,
            cancelText: dialogOptions.cancelText
        }, false);

        container = AJS.$('.ap-dialog-container');

        if(options.url){
            promise = hostContentUtilities.getIframeHtmlForUrl(options.key, options.url, productContext, uiParams);
        } else {
            promise = hostContentUtilities.getIframeHtmlForKey(options.key, productContext, module, uiParams);
        }

        promise
            .done(function(data) {
                var dialogHtml = $(data);
                dialogHtml.addClass('ap-dialog-container');
                container.replaceWith(dialogHtml);
            })
            .fail(function(xhr, status, ex) {
                var title = $("<p class='title' />").text("Unable to load add-on content. Please try again later.");
                container.html("<div class='aui-message error ap-aui-message'></div>");
                container.find(".error").append(title);
                var msg = status + (ex ? ": " + ex.toString() : "");
                container.find(".error").text(msg);
                AJS.log(msg);
            });

        return dialog;
    };
});
