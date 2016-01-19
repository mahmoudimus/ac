connectHost.registerContentResolver.resolveByExtension(function(params){
    function getContentUrl(pluginKey, moduleKey){
        return AJS.contextPath() + "/plugins/servlet/ac/" + encodeURIComponent(pluginKey) + "/" + encodeURIComponent(moduleKey);
    }
    console.log(arguments);
    return $.ajax(getContentUrl(params.extension.addon_key, params.extension.key), {
        dataType: "html",
        data: {
            "plugin-key": params.extension.addon_key,
            // "product-context": JSON.stringify(params.productContext),
            "key": params.extension.key,
            "width": params.width || "100%",
            "height": params.height || "100%",
            "classifier": params.classifier || "raw"
        }
    });

//     var promise = jQuery.Deferred(function(defer){
//         defer.resolve({url: addonDomain + '/iframe-content.html'});
//     }).promise();
// return promise;
});

// (function($, UiParams, context){
//     "use strict";

//     function getContentUrl(pluginKey, moduleKey){
//         return AJS.contextPath() + "/plugins/servlet/ac/" + encodeURIComponent(pluginKey) + "/" + encodeURIComponent(moduleKey);
//     }

//     var contentResolver = {
//         resolveByUrl: function(url) {
//             var promise = jQuery.Deferred(function(defer){
//                 defer.resolve(url);
//             }).promise();

//             return promise;
//         },
//         resolveByParameters: function(params) {
//             return $.ajax(getContentUrl(params.addonKey, params.moduleKey), {
//                 dataType: "html",
//                 data: {
//                     "ui-params": UiParams.encode(params.uiParams),
//                     "plugin-key": params.addonKey,
//                     "product-context": JSON.stringify(params.productContext),
//                     "key": params.moduleKey,
//                     "width": params.width || "100%",
//                     "height": params.height || "100%",
//                     "classifier": params.classifier || "raw"
//                 }
//             });
//         } 
//     };

//     context._AP.contentResolver = contentResolver;


// }(AJS.$, _AP.uiParams, this));