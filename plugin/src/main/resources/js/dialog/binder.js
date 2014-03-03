_AP.require(["dialog/dialog-factory", "host/content", "_uri"], function(dialogFactory, hostContentUtilities, uri) {

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
                options.src = href;

                var contentUrlObj = new uri.init(href);
                if (!options.ns) {
                    options.ns = contentUrlObj.getQueryParamValue('xdm_c').replace('channel-', '');
                }
                if(!options.container){
                    options.container = options.ns;
                }

                //default chrome to be true for backwards compatability
                if(options.chrome === undefined || options.chrome === ""){
                  options.chrome = true;
                }


                options.chrome = true;

                dialogFactory(options).show();
//                simpleDialog(href, options).show();
            };

        hostContentUtilities.eventHandler(action, selector, callback);

    });

});
