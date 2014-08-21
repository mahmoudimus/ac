_AP.require(["inline-dialog/simple", "_dollar", "host/content"], function(simpleInlineDialog, $, hostContentUtilities) {
    var inlineDialogTrigger = '.ap-inline-dialog';

    AJS.toInit(function ($) {
        var action = "click mouseover mouseout",
            callback = function(href, options, eventType){
                var webItemOptions = hostContentUtilities.getOptionsForWebItem(options.bindTo);
                $.extend(options, webItemOptions);
                if(options.onHover !== "true" && eventType !== 'click'){
                    return;
                }
                // don't re-open if already visible.
                if(!options.bindTo.hasClass('active')){
                    simpleInlineDialog(href, options).show();
                }
            };
        hostContentUtilities.eventHandler(action, inlineDialogTrigger, callback);
    });
});