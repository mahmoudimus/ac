_AP.define("analytics", ["_dollar"], function($){
    "use strict";

    var bridgeMethodBlackList = [
        "resize",
        "init"
    ];

    return {
        trackBridgeMethod: function(name){
            if($.inArray(name, bridgeMethodBlackList)){
                return false;
            }
            this.track('bridge.invokemethod', {
                name: name
            });
        },
        track: function(name, data) {
            AJS.trigger('analyticsEvent', {
                name: "connect.addon." + name,
                data: data
            });

            return true;
        }
    };

});
