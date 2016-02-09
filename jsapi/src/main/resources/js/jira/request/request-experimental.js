(function ($, require) {
    "use strict";
    require(["ac/request"], function (request) {
        request.setExperimentify(function(ajaxOptions) {
            ajaxOptions.headers["X-ExperimentalApi"] = "opt-in";
            return ajaxOptions;
        })
    });
})(AJS.$, require);