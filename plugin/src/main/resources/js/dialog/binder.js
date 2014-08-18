_AP.require(["dialog/main", "host/content", "_uri", "dialog/dialog-factory"], function(dialog, hostContentUtilities, uri, dialogFactory) {

  /**
   * Binds all elements with the class "ap-dialog" to open dialogs.
   * TODO: document options
   */
    AJS.toInit(function ($) {

        var action = "click",
            selector = ".ap-dialog",
            initWithUrl = function(href, options){

                var webItemOptions = hostContentUtilities.getOptionsForWebItem(options.bindTo);
                // this is a dialog-page (xml descriptor) or a web item with target=(dialog|inlineDialog)
                var dialogPageMatch = href.match(/\/servlet\/atlassian\-connect\/([\w-]+)\/([\w-]+)/) || href.match(/\/servlet\/ac\/([\w-]+)\/([\w-]+)/);
                if(dialogPageMatch){
                    var dialogPageOptions = {
                        key: dialogPageMatch[1],
                        moduleKey: dialogPageMatch[2],
                        chrome: true
                    };

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

                // webitem target options can sometimes be sent as strings.
                if(typeof options.chrome === "string"){
                    options.chrome = (options.chrome.toLowerCase() === "false") ? false : true;
                }

                //default chrome to be true for backwards compatibility with webitems
                if(options.chrome === undefined){
                  options.chrome = true;
                }

                dialog.create(options);
            };

        var handleJwtFetchFailure = function (href, options) {
            // TODO: log? show a message to the user?
        };

        /**
         * Will fetch a fresh JWT so that we don't get expired tokens.
         * @param successHandler will be invoked with (href, options) arguments if fetching a fresh JWT succeeds
         * @param errorHandler will be invoked with (href, options) arguments if fetching a fresh JWT fails
         */
        function refreshJwt(successHandler, errorHandler) {

        }

        var clickHandler = function(href, options) {
            refreshJwt(initWithUrl, handleJwtFetchFailure);
        };

        hostContentUtilities.eventHandler(action, selector, clickHandler);

    });

});
