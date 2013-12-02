_AP.require(["inline-dialog/simple", "_dollar", "host/content"], function(simpleInlineDialog, $, hostContentUtilities) {

    var inlineDialogTrigger = '.ap-inline-dialog';

    AJS.toInit(function ($) {
        var action = "click",
            callback = function(href, options){
                simpleInlineDialog(href, options).show();
            };

        hostContentUtilities.eventHandler(action, inlineDialogTrigger, callback);
    });

});
