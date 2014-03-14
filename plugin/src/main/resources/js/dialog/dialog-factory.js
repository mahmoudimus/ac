_AP.define("dialog/dialog-factory", ["_dollar", "dialog/simple-dialog", 'host/content'], function($, dialog, hostContentUtilities) {
    //might rename this, it opens a dialog by first working out the url (used for javascript opening a dialog).
    return function(options, dialogOptions, productContextJson) {
        var promise,
        container,
        dialogObj;

        dialog.create({
            ns: dialogOptions.key,
            chrome: dialogOptions.chrome || options.chrome,
            header: dialogOptions.header,
            width: dialogOptions.width,
            height: dialogOptions.height,
            size: dialogOptions.size
        });

        container = AJS.$('.ap-dialog-container');

        if(dialogOptions.url){
            promise = hostContentUtilities.getIframeHtmlForUrl(options.key, dialogOptions, { dlg: 1 });
        } else {
            promise = hostContentUtilities.getIframeHtmlForKey(options.key, productContextJson, dialogOptions, { dlg: 1 });
        }

        promise
          .done(function(data) {
            container.replaceWith($(data));
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
