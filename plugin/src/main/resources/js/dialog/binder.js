_AP.require(["dialog/simple-dialog", "host/content", "_uri", "dialog/dialog-factory"], function(simpleDialog, hostContentUtilities, uri, dialogFactory) {

  /**
   * Binds all elements with the class "ap-dialog" to open dialogs.
   * TODO: document options
   */
    AJS.toInit(function ($) {

        var action = "click",
            selector = ".ap-dialog",
            callback = function(href, options){

                var webItemOptions = hostContentUtilities.getOptionsForWebItem(options.bindTo);
                //this is a dialog-page (xml descriptor)
                var dialogPageMatch = href.match(/\/servlet\/atlassian\-connect\/([\w-]+)\/([\w-]+)/);
                if(dialogPageMatch){
                    var dialogPageOptions = {
                        key: dialogPageMatch[1],
                        chrome: true
                    };
                    options.key = dialogPageMatch[2];
                    dialogFactory(dialogPageOptions, options);
                    return;
                }

                $.extend(options, webItemOptions);
                options.src = href;

                var contentUrlObj = new uri.init(href);
                if (!options.ns) {
                    options.ns = contentUrlObj.getQueryParamValue('xdm_c').replace('channel-', '');
                }
                if(!options.container){
                    options.container = options.ns;
                }

                //default chrome to be false for backwards compatability with webitems
                if(options.chrome === undefined || options.chrome === ""){
                  options.chrome = false;
                }

                simpleDialog.create(options);
            };

        hostContentUtilities.eventHandler(action, selector, callback);

    });

});
