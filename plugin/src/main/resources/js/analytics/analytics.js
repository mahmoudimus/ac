_AP.define("analytics/analytics", ["_dollar"], function($){
    "use strict";

    /**
     * Blacklist certain bridge functions from being sent to analytics
     * @const
     * @type {Array}
     */
    var BRIDGEMETHODBLACKLIST = [
        "resize",
        "init"
    ];

    /**
     * Timings beyond 20 seconds (connect's load timeout) will be clipped to an X.
     * @const
     * @type {int}
     */
    var THRESHOLD = 20000;

    /**
     * Trim extra zeros from the load time.
     * @const
     * @type {int}
     */
    var TRIMPPRECISION = 100;

    function time() {
        return window.performance && window.performance.now ? window.performance.now() : new Date().getTime();
    }

    function Analytics(addonKey, moduleKey) {
        var metrics = {};
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
        this.iframePerformance = {
            start: function(){
                metrics.startLoading = time();
            },
            end: function(){
                var value = time() - metrics.startLoading;
                proto.track('iframe.performance.load', {
                    addonKey: addonKey,
                    moduleKey: moduleKey,
                    value: value > THRESHOLD ? 'x' : Math.ceil((value) / TRIMPPRECISION)
                });
                delete metrics.startLoading;
            },
            timeout: function(){
                proto.track('iframe.performance.timeout', {
                    addonKey: addonKey,
                    moduleKey: moduleKey
                });
                //track an end event during a timeout so we always have complete start / end data.
                this.end();
            },
            // User clicked cancel button during loading
            cancel: function(){
                proto.track('iframe.performance.cancel', {
                    addonKey: addonKey,
                    moduleKey: moduleKey
                });
            }
        };

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
        if($.inArray(name, BRIDGEMETHODBLACKLIST) !== -1){
            return false;
        }
        this.track('bridge.invokemethod', {
            name: name,
            addonKey: this.addonKey,
            moduleKey: this.moduleKey
        });
    };

    return {
        get: function (addonKey, moduleKey) {
            return new Analytics(addonKey, moduleKey);
        }
    };


});
