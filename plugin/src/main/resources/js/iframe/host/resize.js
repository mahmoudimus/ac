_AP.define("resize", ["_dollar", "_rpc"], function ($, rpc) {
    "use strict";

    var resize = function(iframe, width, height){
        $(iframe).css({width: width, height: height});
    };

    rpc.extend(function(config){

        function resizeHandler() {
            var height = $(document).height() - AJS.$("#header > nav").outerHeight() - AJS.$("#footer").outerHeight() - 20;
            $(config.iframe).css({width: "100%", height: height + "px"});
        }

        return {
            init: function(state){
            },
            internals: {
                resize: function(width, height){
                    console.log(this.id);
                    resize(this.iframe, width, height);
                },
                sizeToParent: _.debounce(function() {
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
