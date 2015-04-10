(function($, define){
    "use strict";
    $(document).ready(function() {

        $("body").on("resized",".ap-container", function(e, dimensions) {
            //AG.DashboardManager.getLayout().refresh();
            var gadget = new AG.InlineGadgetAPI(AJS.$(e.target).parents(".gadget-inline")[0]);
            var gadgetId = AJS.$(e.target).parents(".gadget-inline").first().attr("id");
            var gadgets = AG.DashboardManager.getLayout().getGadgets();
            var result = gadgets.filter(function(g) {
                return "gadget-".concat(g.getId()) == gadgetId;
            });
            result[0].resize();
        });
    });
})(AJS.$, define);