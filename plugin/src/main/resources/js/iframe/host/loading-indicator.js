_AP.define("loading-indicator", ["_dollar", "_rpc", "host/_status_helper"], function ($, rpc, statusHelper) {
    "use strict";

    rpc.extend(function (config) {
        return {
            init: function (state, xdm) {
                var $home = $(state.iframe).closest(".ap-container");
                statusHelper.showLoadingStatus($home, 0);
                xdm.timeout = setTimeout(function(){
                    xdm.timeout = null;
                    statusHelper.showloadTimeoutStatus($home);
                    var $timeout = $home.find(".ap-load-timeout");
                    $timeout.find("a.ap-btn-cancel").click(function () {
                        statusHelper.showLoadErrorStatus($home);
                        this.analytics.iframePerformance.timeout();
                        //state.iframe.trigger(isDialog ? "ra.dialog.close" : "ra.iframe.destroy");
                    });
                }, 20000);
            },
            internals: {
                init: function() {
                    this.analytics.iframePerformance.end();
                    var $home = $(this.iframe).closest(".ap-container");
                    statusHelper.showLoadedStatus($home);

                    clearTimeout(this.timeout);
                    // Let the integration tests know the iframe has loaded.
                    $home.find(".ap-content").addClass("iframe-init");
                }
            }
        };

    });

});
