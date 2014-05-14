_AP.define("analytics/analytics", ["_dollar"], function($){
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

    function Analytics(addonKey, moduleKey) {
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
        this.metrics = {};
    }

    var proto = Analytics.prototype;

    proto.getKey = function () {
        return this.addonKey + ':' + this.moduleKey;
    };

    proto.track = function (name, data) {
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
    };

    proto.trackBridgeMethod = function(name){
        if($.inArray(name, bridgeMethodBlackList) !== -1){
            return false;
        }
        track('bridge.invokemethod', {
            name: name,
            addonKey: this.addonKey,
            moduleKey: this.moduleKey
        });
    };

    proto.iframePerformance = {
        start: function(){
            metrics.startLoading = time();
        },
        end: function(){
            var value = time() - metrics.startLoading;
            delete metrics.startLoading;

            this.track('iframe.performance.load', {
                addonKey: this.addonKey,
                moduleKey: this.moduleKey,
                value: value > THRESHOLD ? 'x' : Math.ceil((value) / TRIMPPRECISION)
            });
        },
        timeout: function(){
            track('iframe.performance.timeout', {
                addonKey: this.addonKey,
                moduleKey: this.moduleKey
            });
            //track an end event during a timeout so we always have complete start / end data.
            this.end();
        },
        // User clicked cancel button during loading
        cancel: function(){
            track('iframe.performance.cancel', {
                addonKey: this.addonKey,
                moduleKey: this.moduleKey
            });
        }
    };

    return {
        get: function (addonKey, moduleKey) {
            return new Analytics(addonKey, moduleKey);
        }
    };


});
