_AP.require(["dialog/simple", "host/content"], function(simpleDialog, hostContentUtilities) {

  /**
   * Binds all elements with the class "ap-dialog" to open dialogs.
   * TODO: document options
   */
    AJS.toInit(function ($) {
        var action = "click",
            selector = ".ap-dialog",
            callback = function(href, options){
                var webItemOptions = hostContentUtilities.getOptionsForWebItem(options.bindTo);
                $.extend(options, webItemOptions);
                simpleDialog(href, options).show();
            };

        hostContentUtilities.eventHandler(action, selector, callback);

    });

});
