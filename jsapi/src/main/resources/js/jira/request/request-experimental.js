(function ($, require) {
    "use strict";
    require(["ac/request"], function (request) {
        request.defineSetExperimentalHeader(function(headers) {
            headers["X-ExperimentalApi"] = "opt-in";
            return headers;
        })
    });
})(AJS.$, require);