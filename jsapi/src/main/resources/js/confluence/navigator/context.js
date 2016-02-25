(function($, require){
    "use strict";
    require(["ac/navigator","confluence/api/navigator-context"], function(navigator, confluenceContext){
        navigator.setContextFunction(confluenceContext.getCurrent);
    });

})(AJS.$, require);
