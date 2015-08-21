(function($, require){
    "use strict";
    require(["ac/jira/agile-board", 'connect-host'], function(workflowPostFunction, _AP) {

        _AP.extend(function () {
            return {
                internals: {
                    refreshWorkModeView: function (callback) {
                        GH.RapidBoard.reload()
                    }
                }
            };
        });
    });
})(AJS.$, require);
