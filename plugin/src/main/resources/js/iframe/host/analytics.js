_AP.define("analytics", ["_dollar"], function($){
    "use strict";

    var bridgeMethodBlackList = [
        "resize",
        "init"
    ];


    return {
        trackBridgeMethod: function(name, addonKey, channel){
            if($.inArray(name, bridgeMethodBlackList) !== -1){
                return false;
            }

            var moduleKey = channel.replace('channel-', '');
            this.track('bridge.invokemethod', {
                name: name,
                addonKey: addonKey,
                moduleKey: moduleKey
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
