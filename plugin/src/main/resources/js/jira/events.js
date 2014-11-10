(function(define, $){
    "use strict";
    define("ac/jira/events", ['connect-host'], function(_AP){
        var module = {
            refreshIssuePage: function(){
                try {
                    JIRA.trigger(JIRA.Events.REFRESH_ISSUE_PAGE, [JIRA.Issue.getIssueId()]);
                } catch (e){
                    throw new Error("Failed to refresh the issue page");
                }
            }
        };

        _AP.extend(function () {
            return {
                internals: {
                    triggerJiraEvent: function () {
                        module.refreshIssuePage();
                    }
                }
            };
        });
        return module;
    });

})(define, AJS.$);
