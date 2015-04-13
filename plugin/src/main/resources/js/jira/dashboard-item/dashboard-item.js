(function($, define){
    "use strict";

    define("atlassian-connect/connect-dashboard-item", [], function() {
        var Gadget = function(API) {
            this.API = API;
        };

        Gadget.prototype.renderEdit = function(element, preferences) {
            // send the event to the dashboard-item
        };
        return Gadget;
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