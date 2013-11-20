_AP.require(["inline_dialog/simple", "_dollar", "host/content"], function(simpleInlineDialog, $, hostContentUtilities) {

    AJS.toInit(function ($) {
        var action = "click",
            selector = ".ap-inline-dialog",
            callback = function(href, options){
                simpleInlineDialog(href, options).show();
            };
        hostContentUtilities.eventHandler(action, selector, callback);
    });

});
