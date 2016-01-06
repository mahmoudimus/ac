(function($, require){
    "use strict";
    require(["ac/navigator","confluence/api/navigator-targets"], function(navigator, targets){
        navigator.setContextFunction(targets.getCurrent);
    });

})(AJS.$, require);
