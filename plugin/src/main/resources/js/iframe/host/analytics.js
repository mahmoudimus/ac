_AP.define("analytics", ["_dollar"], function($){
    "use strict";

    var bridgeMethodBlackList = [
        "resize",
        "init"
    ];

    var THRESHOLD = 12000; // Timings above this threshold will be clipped to an 'x' value.

    var metrics = {};

    function time() {
        return window.performance && window.performance.now ? window.performance.now() : new Date().getTime();
    }

    function getKey (addonKey, moduleKey) {
        return addonKey + '$$' + moduleKey;
    }

    function track (name, data) {
        AJS.trigger('analyticsEvent', {
            name: "connect.addon." + name,
            data: data
        });

        return true;
    }

    return {
        iframePerformance: {
            start: function(addonKey, moduleKey){
                metrics[getKey()] = time();
            },
            end: function(addonKey, moduleKey){
                var key = getKey(),
                value = time() - metrics[key];

                metrics[key] = null;

                track('iframe.performance.load', {
                    addonKey: addonKey,
                    moduleKey: moduleKey,
                    value: value > THRESHOLD ? 'x' : Math.ceil((value) / 100)
                });
            },
            timeout: function(addonKey, moduleKey){
                track('iframe.performance.timeout', {
                    addonKey: addonKey,
                    moduleKey: moduleKey
                });
                metrics[key] = null;
            }
        },
        trackBridgeMethod: function(name, addonKey, channel){
            if($.inArray(name, bridgeMethodBlackList) !== -1){
                return false;
            }

            var moduleKey = channel.replace('channel-', '');
            track('bridge.invokemethod', {
                name: name,
                addonKey: addonKey,
                moduleKey: moduleKey
            });
        },
        track: track
    };

});
