(function($, define){
    "use strict";

    require(['connect-host'], function(_AP) {

        _AP.extend(function () {
            return {
                init: function (state, xdm) {
                    // register handle for the edit button in jira (if needed)
                    $(xdm.iframe).on('gadgetEdit', function(){
                        xdm.triggerGadgetEdit();
                    });
                },
                stubs: ["triggerGadgetEdit"]
            };
        });
    });

    define('atlassian-connect/connect-dashboard-item', function(){
        return function(){
            return {
                renderEdit: function($element){
                    $element.find('iframe').trigger('gadgetEdit');
                }
            };
        };
    });


    $(document).ready(function() {

        $("body").on("resized",".ap-container", function(e, dimensions) {
            var inlineGadgetContainer = AJS.$(e.target).parents(".gadget-inline").first();
            var gadgetId = inlineGadgetContainer.attr("id");
            var gadget = new AG.InlineGadgetAPI(inlineGadgetContainer);
            var layout = AG.DashboardManager.getLayout();
            var result = layout.getGadgets().filter(function(g) {
                return "gadget-".concat(g.getId()) == gadgetId;
            });
            result[0].resize();
            layout.refresh();
        });

    });


})(AJS.$, define);