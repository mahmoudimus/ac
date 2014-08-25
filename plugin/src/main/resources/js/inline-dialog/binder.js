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
                // don't repeatedly open if already visible as dozens of mouse-over events are fired in quick succession
                if (options.onHover === true && options.bindTo.hasClass('active')) {
                    return;
                }
                simpleInlineDialog(href, options).show();
            };
        hostContentUtilities.eventHandler(action, inlineDialogTrigger, callback);
    });
});