(function(define, extend, $){
    "use strict";

    var module = {
        refreshIssuePage: function(){
            try {
                JIRA.trigger(JIRA.Events.REFRESH_ISSUE_PAGE, [JIRA.Issue.getIssueId()]);
            } catch (e){
                throw new Error("Failed to refresh the issue page");
            }
        }
    };

    extend(function () {
        return {
            internals: {
                triggerJiraEvent: function () {
                    module.refreshIssuePage();
                }
            }
        };
    });

})(define, _AP.extend, AJS.$);
