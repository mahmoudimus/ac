(function($, require){
    "use strict";
    require(["ac/navigator"], function(navigator){
        var routes = {
            "dashboard"    : "",
            "contentview"  : "/pages/viewpage.action?pageId={contentId}",
            "contentedit"  : "/pages/resumedraft.action?draftId={draftId}&draftShareId={shareToken}",
            "spacetools"   : "/spaces/viewspacesummary.action?key={spaceKey}",
            "spaceview"    : "/display/{spaceKey}",
            "userprofile"  : "/display/~{username}"
        };
        navigator.setRoutes(routes);
    });

})(AJS.$, require);
