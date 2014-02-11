_AP.require(["inline-dialog/simple", "_dollar", "host/content"], function(simpleInlineDialog, $, hostContentUtilities) {
var inlineDialogTrigger = '.ap-inline-dialog';
    AJS.toInit(function ($) {
        var action = "click mouseover mouseout",
            callback = function(href, options, eventType){
                var webItemOptions = hostContentUtilities.getOptionsForWebItem(
                    "inlineDialog",
                    options.addonKey, //broken until ACDEV-996 is fixed
                    options.bindTo.attr('id'));

                $.extend(options, webItemOptions);
                if(options.onHover !== "true" && eventType !== 'click'){
                    return;
                }
                simpleInlineDialog(href, options).show();
            };
        hostContentUtilities.eventHandler(action, inlineDialogTrigger, callback);
    });
});