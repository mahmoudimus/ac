_AP.require(["dialog/main", "host/content", "_uri", "dialog/dialog-factory"], function (dialog, hostContentUtilities, uri, dialogFactory) {

    /**
     * Binds all elements with the class "ap-dialog" to open dialogs.
     * TODO: document options
     */
    AJS.toInit(function ($) {

        var action = "click",
            selector = ".ap-dialog",
            callback = function (href, options) {

                var webItemOptions = hostContentUtilities.getOptionsForWebItem(options.bindTo),
                    moduleKey = hostContentUtilities.getWebItemModuleKey(options.bindTo),
                    addonKey = hostContentUtilities.getWebItemPluginKey(options.bindTo);

                $.extend(options, webItemOptions);

                if (!options.ns) {
                    options.ns = moduleKey;
                }
                if (!options.container) {
                    options.container = options.ns;
                }

                // webitem target options can sometimes be sent as strings.
                if (typeof options.chrome === "string") {
                    options.chrome = (options.chrome.toLowerCase() === "false") ? false : true;
                }

                //default chrome to be true for backwards compatibility with webitems
                if (options.chrome === undefined) {
                    options.chrome = true;
                }

                dialogFactory({
                    key: addonKey,
                    moduleKey: moduleKey,
                    triggerUri: new uri.init(href)
                }, options);

                // dialog.create(options);
            };

        hostContentUtilities.eventHandler(action, selector, callback);

    });

});