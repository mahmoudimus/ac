(function($, define){
    "use strict";
    var gadgetEditTrigger;

    require(['connect-host'], function(_AP) {

        _AP.extend(function () {
            return {
                init: function (state, xdm) {
                    // register handle for the edit button in jira (if needed)
                    gadgetEditTrigger = function(){
                        alert('i was triggered');
                        xdm.setGadgetEdit();
                    };
                },
                stubs: ["setGadgetEdit"]
            };
        });
    });

    define('ac/gadget/trigger', function(){
        return {
            renderEdit: gadgetEditTrigger
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