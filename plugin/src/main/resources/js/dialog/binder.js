_AP.require(["dialog/dialog-factory", "host/content"], function(dialogFactory, hostContentUtilities) {

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
                //default chrome to be true for backwards compatability
                if(options.chrome === undefined || options.chrome === ""){
                  options.chrome = true;
                }
                dialogFactory(href, options).show();
//                simpleDialog(href, options).show();
            };

        hostContentUtilities.eventHandler(action, selector, callback);

    });

});
