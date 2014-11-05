define("host/content-resolver", ["_dollar", "_ui-params"], function($, UiParams){
    "use strict";

    function getContentUrl(pluginKey, moduleKey){
        return AJS.contextPath() + "/plugins/servlet/ac/" + encodeURIComponent(pluginKey) + "/" + encodeURIComponent(moduleKey);
    }

    return {
        resolveByUrl: function(url) {
            var promise = jQuery.Deferred(function(defer){
                defer.resolve(url);
            }).promise();

            return promise;
        },
        resolveByParameters: function(params) {
            return $.ajax(getContentUrl(params.addonKey, params.moduleKey), {
                dataType: "html",
                data: {
                    "ui-params": UiParams.encode(params.uiParams),
                    "plugin-key": params.addonKey,
                    "product-context": JSON.stringify(params.productContext),
                    "key": params.moduleKey,
                    "width": params.width || "100%",
                    "height": params.height || "100%",
                    "classifier": params.classifier || "raw"
                }
            });
        } 
    };

});

// add it to the global object. In future, this pattern might change.
window._AP.contentResolver = require("host/content-resolver");
