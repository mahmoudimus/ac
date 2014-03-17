_AP.define("dialog/dialog-factory", ["_dollar", "dialog/simple-dialog", 'host/content'], function($, dialog, hostContentUtilities) {
    //might rename this, it opens a dialog by first working out the url (used for javascript opening a dialog).
    /**
    * opens a dialog by sending the add-on and module keys back to the server for signing.
    * Used by dialog-pages, confluence macros and opening from javascript.
    * @param {Object} options for passing to AP.create
    * @param {Object} dialog options (width, height, etc)
    * @param {String} productContextJson pass context back to the server
    */
    return function(options, dialogOptions, productContextJson) {
        var promise,
        container,
        module = {key: options.moduleKey};

        dialog.create({
            ns: options.moduleKey,
            chrome: dialogOptions.chrome || options.chrome,
            header: dialogOptions.header,
            width: dialogOptions.width,
            height: dialogOptions.height,
            size: dialogOptions.size
        }, false);
        container = AJS.$('.ap-dialog-container');

        if(dialogOptions.url){
            promise = hostContentUtilities.getIframeHtmlForUrl(options.key, options.url, { dlg: 1 });
        } else {
            promise = hostContentUtilities.getIframeHtmlForKey(options.key, productContextJson, module, { dlg: 1 });
        }
        promise
          .done(function(data) {
            var dialogHtml = $(data);
            dialogHtml.addClass('ap-dialog-container');
            container.replaceWith(dialogHtml);
          })
          .fail(function(xhr, status, ex) {
            var title = "Unable to load plugin content. Please try again later.";
            container.html("<div class='aui-message error' style='margin: 10px'></div>");
            container.find(".error").append("<p class='title'>" + title + "</p>");
            var msg = status + (ex ? ": " + ex.toString() : "");
            container.find(".error").append(msg);
            AJS.log(msg);
          });

        return dialog;
    };
});
