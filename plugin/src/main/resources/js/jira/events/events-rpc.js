(function(require, $){
    "use strict";
    require(["connect-host", "ac/jira/events"], function(_AP, jiraEvents){
        _AP.extend(function () {
            return {
                internals: {
                    triggerJiraEvent: function () {
                        jiraEvents.refreshIssuePage();
                    }
                }
            };
        });
    });

})(require, AJS.$);
