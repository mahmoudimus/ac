_AP.define("loading-indicator", ["_dollar", "_rpc", "host/_status_helper"], function ($, rpc, statusHelper) {
    "use strict";


    rpc.extend(function(config){
 
        var timeout;

        return {
            init: function(state){
                var $home = $(config.iframe).closest(".ap-container");
                statusHelper.showLoadingStatus($home, 0);

                timeout = setTimeout(function(){
                    timeout = null;
                    statusHelper.showloadTimeoutStatus($home);
                    var $timeout = $home.find(".ap-load-timeout");
                    $timeout.find("a.ap-btn-cancel").click(function () {
                        statusHelper.showLoadErrorStatus($home);
                        //state.iframe.trigger(isDialog ? "ra.dialog.close" : "ra.iframe.destroy");
                    });
                }, 2000);
            },
            internals: {
                init: function(state){
                    var $home = $(config.iframe).closest(".ap-container");
                    statusHelper.showLoadedStatus($home);
                    clearTimeout(timeout);
                }
            }
        };
    });

});
