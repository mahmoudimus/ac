_AP.define("resize", ["_dollar", "_rpc"], function ($, rpc) {
    "use strict";
    // a simplified version of underscore's debounce
    function debounce(fn, wait) {
        var timeout;
        return function() {
            var ctx = this,
                args = [].slice.call(arguments);
            function later() {
                timeout = null;
                fn.apply(ctx, args);
            }
            if (timeout) {
                clearTimeout(timeout);
            }
            timeout = setTimeout(later, wait || 50);
        };
    }

    rpc.extend(function(config){

        var connectModuleData;

        function resizeHandler() {
            var height = $(document).height() - AJS.$("#header > nav").outerHeight() - AJS.$("#footer").outerHeight() - 20;
            $(config.iframe).css({width: "100%", height: height + "px"});
        }

        return {
            init: function(state){
                connectModuleData = state;
            },
            internals: {
                resize: debounce(function(width, height){
                    $(config.iframe).css({width: width, height: height});
                }),
                sizeToParent: debounce(function() {
                    // sizeToParent is only available for general-pages
                    if (connectModuleData.isGeneral) {
                        // This adds border between the iframe and the page footer as the connect addon has scrolling content and can't do this
                        $(config.iframe).addClass("full-size-general-page");
                        $(window).on('resize', resizeHandler);
                        resizeHandler();
                    }
                    else {
                        // This is only here to support integration testing
                        // see com.atlassian.plugin.connect.test.pageobjects.RemotePage#isNotFullSize()
                        $(config.iframe).addClass("full-size-general-page-fail");
                    }
                }),
            }
        };
    });

});
