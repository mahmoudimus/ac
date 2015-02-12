(function($, define){
    "use strict";
    define("ac/jira/events", ['connect-host'], function(_AP){
        return {
            refreshIssuePage: function(){
                try {
                    JIRA.trigger(JIRA.Events.REFRESH_ISSUE_PAGE, [JIRA.Issue.getIssueId()]);
                } catch (e){
                    throw new Error("Failed to refresh the issue page");
                }
            }
        };
    });

})(AJS.$, define);
