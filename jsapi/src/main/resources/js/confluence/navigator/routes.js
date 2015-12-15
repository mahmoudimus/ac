(function($, require){
    "use strict";
    require(["ac/navigator"], function(navigator){
        var routes = {
            "dashboard"    : "",
            "contentview"  : "/pages/viewpage.action?pageId={contentId}",
            "contentedit"  : "/edit{contentType}.action?pageId={contentId}",
            "spacetools"   : "/spaces/viewspacesummary.action?key={spaceKey}",
            "spaceview"    : "/display/{spaceKey}",
            "userprofile"  : "/display/~{username}"
        };
        navigator.setRoutes(routes);
    });

})(AJS.$, require);
