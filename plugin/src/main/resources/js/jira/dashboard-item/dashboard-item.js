(function($, define){
    "use strict";

    require(['connect-host'], function(_AP) {

        _AP.extend(function () {
            return {
                init: function (state, xdm) {
                    // register handle for the edit button in jira (if needed)
                    $(xdm.iframe).on('dashboardItemEdit', function(){
                        xdm.triggerDashboardItemEdit();
                    });
                },
                internals: {
                    closeDashboardItemEdit: function () {
                        var that = this;
                        new AG.InlineGadgetAPI(AJS.$(that.iframe).parents(".gadget-inline")).closeEdit();
                    },
                    setDashboardItemTitle: function(title) {
                        var that = this;
                        // TODO this should be replaced with a valid gadget API
                        var p = AJS.$(AJS.$(that.iframe).parents('.gadget-container').find('h3.dashboard-item-title')[0]);
                        p.text(title);
                    }
                },
                stubs: ["triggerDashboardItemEdit"]
            };
        });
    });

    define('atlassian-connect/connect-dashboard-item', function() {
        return function(){
            return {
                renderEdit: function($element){
                    $element.find('iframe').trigger('dashboardItemEdit');
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