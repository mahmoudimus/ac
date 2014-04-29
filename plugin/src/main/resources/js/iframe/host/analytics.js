_AP.define("host/analytics", ["_dollar"], function($){
    "use strict";

    var bridgeMethodBlackList = [
        "resize",
        "init"
    ];

    var THRESHOLD = 20000; // Timings beyond 20 seconds (connect's load timeout) will be clipped to an X.
    var TRIMPPRECISION = 100; // Trim extra zeros from the load time.

    var metrics = {};

    function time() {
        return window.performance && window.performance.now ? window.performance.now() : new Date().getTime();
    }

    function getKey (addonKey, moduleKey) {
        return addonKey + ':' + moduleKey;
    }

    function track (name, data) {
        var prefixedName = "connect.addon." + name;

        if(AJS.Analytics){
            AJS.Analytics.triggerPrivacyPolicySafeEvent(prefixedName, data);
        } else if(AJS.trigger) {
            // BTF fallback
            AJS.trigger('analyticsEvent', {
                name: prefixedName,
                data: data
            });
        } else {
            return false;
        }

        return true;
    }

    return {
        getKey: getKey,
        iframePerformance: {
            start: function(addonKey, moduleKey){
                metrics[getKey(addonKey, moduleKey)] = time();
            },
            end: function(addonKey, moduleKey){
                var key = getKey(addonKey, moduleKey),
                value = time() - metrics[key];

                metrics[key] = null;

                track('iframe.performance.load', {
                    addonKey: addonKey,
                    moduleKey: moduleKey,
                    value: value > THRESHOLD ? 'x' : Math.ceil((value) / TRIMPPRECISION)
                });
            },
            timeout: function(addonKey, moduleKey){
                var key = getKey(addonKey, moduleKey);
                track('iframe.performance.timeout', {
                    addonKey: addonKey,
                    moduleKey: moduleKey
                });
                //track an end event during a timeout so we always have complete start / end data.
                this.end(addonKey, moduleKey);
                metrics[key] = null;
            },
            // User clicked cancel button during loading
            cancel: function(addonKey, moduleKey){
                track('iframe.performance.cancel', {
                    addonKey: addonKey,
                    moduleKey: moduleKey
                });
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
