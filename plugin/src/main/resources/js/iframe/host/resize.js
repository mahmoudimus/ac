_AP.define("resize", ["_dollar", "_rpc"], function ($, rpc) {
    "use strict";

    rpc.extend(function () {

        function resizeHandler(iframe) {
            var height = $(document).height() - $("#header > nav").outerHeight() - $("#footer").outerHeight() - 20;
            $(iframe).css({width: "100%", height: height + "px"});
        }

        return {
            init: function (config, xdm) {
                xdm.resize = _.debounce(function resize ($, width, height) {
                    $(this.iframe).css({width: width, height: height});
                });
            },
            internals: {
                resize: function(width, height) {
                    if(!this.uiParams.isDialog){
                        this.resize($, width, height);
                    }
                },
                sizeToParent: _.debounce(function() {
                    // sizeToParent is only available for general-pages
                    if (this.uiParams.isGeneral) {
                        // This adds border between the iframe and the page footer as the connect addon has scrolling content and can't do this
                        $(this.iframe).addClass("full-size-general-page");
                        $(window).on('resize', function(){
                            resizeHandler(this.iframe);
                        });
                        resizeHandler(this.iframe);
                    }
                    else {
                        // This is only here to support integration testing
                        // see com.atlassian.plugin.connect.test.pageobjects.RemotePage#isNotFullSize()
                        $(this.iframe).addClass("full-size-general-page-fail");
                    }
                })
            }
        };
    });

});
