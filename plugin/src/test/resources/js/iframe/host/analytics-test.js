
(function(){
    define(["iframe/host/analytics", "iframe/host/_dollar"], function() {
        _AP.require(["host/analytics"], function(analytics){
            module("Analytics", {

                setup: function() {
                    this.triggerSpy = sinon.spy();
                    AJS.trigger = this.triggerSpy;

                    this.clock = sinon.useFakeTimers();
                },
                teardown: function() {
                    delete AJS.trigger;
                    this.clock.restore();
                }
            });


            test("getKey returns addonKey$$moduleKey", function() {
                var addonKey = "myaddonkey",
                moduleKey = "myModuleKey",
                response = analytics.getKey(addonKey, moduleKey);
                equal(response, addonKey + '$$' + moduleKey);
            });

            test("trackBridgeMethod triggers an analytics event", function() {
                var addonKey = "myaddonkey",
                moduleKey = "myModuleKey",
                functionName = "bridgeFunction";

                analytics.trackBridgeMethod(functionName, addonKey, moduleKey);
                equal(this.triggerSpy.args[0][0], "analyticsEvent");
            });

            test("trackBridgeMethod sends the method name", function() {
                var addonKey = "myaddonkey",
                moduleKey = "myModuleKey",
                functionName = "bridgeFunction";

                analytics.trackBridgeMethod(functionName, addonKey, moduleKey);
                equal(this.triggerSpy.args[0][1].data.name, functionName);
            });

            test("trackBridgeMethod sends addon / module keys", function() {
                var addonKey = "myaddonkey",
                moduleKey = "myModuleKey",
                functionName = "bridgeFunction";

                analytics.trackBridgeMethod(functionName, addonKey, moduleKey);
                equal(this.triggerSpy.args[0][1].data.moduleKey, moduleKey);
                equal(this.triggerSpy.args[0][1].data.addonKey, addonKey);
            });

            test("iframePerformance end triggers an analytics event", function() {
                var addonKey = "myaddonkey",
                moduleKey = "myModuleKey";

                analytics.iframePerformance.start(addonKey, moduleKey);
                analytics.iframePerformance.end(addonKey, moduleKey);
                equal(this.triggerSpy.args[0][0], "analyticsEvent");
            });

            test("iframePerformance end analytics event includes addon / module keys", function() {
                var addonKey = "myaddonkey",
                moduleKey = "myModuleKey";
                analytics.iframePerformance.start(addonKey, moduleKey);
                analytics.iframePerformance.end(addonKey, moduleKey);
                equal(this.triggerSpy.args[0][1].data.addonKey, addonKey);
                equal(this.triggerSpy.args[0][1].data.moduleKey, moduleKey);
            });

            test("iframePerformance end analytics event includes time value", function() {
                var addonKey = "myaddonkey",
                moduleKey = "myModuleKey";
                analytics.iframePerformance.start(addonKey, moduleKey);
                analytics.iframePerformance.end(addonKey, moduleKey);
                ok(this.triggerSpy.args[0][1].data.value > 0);
                ok(Number.isFinite(this.triggerSpy.args[0][1].data.value), "performance value must be a number");
            });


            test("iframePerformance timeout triggers an analytics event", function() {
                var addonKey = "myaddonkey",
                moduleKey = "myModuleKey";

                analytics.iframePerformance.start(addonKey, moduleKey);
                analytics.iframePerformance.timeout(addonKey, moduleKey);
                equal(this.triggerSpy.args[0][0], "analyticsEvent");
            });

            test("iframePerformance timeout analytics event includes addon / module keys", function() {
                var addonKey = "myaddonkey",
                moduleKey = "myModuleKey";
                analytics.iframePerformance.start(addonKey, moduleKey);
                analytics.iframePerformance.timeout(addonKey, moduleKey);
                equal(this.triggerSpy.args[0][1].data.addonKey, addonKey);
                equal(this.triggerSpy.args[0][1].data.moduleKey, moduleKey);
            });
            





        });

    });

})();
