_AP.define("jira/event", ["_dollar"], function($) {

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
