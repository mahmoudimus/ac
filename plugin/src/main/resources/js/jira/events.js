_AP.define("jira/events", ["_dollar", "_rpc"], function($, rpc) {

    var module = {
        refreshIssuePage: function(){
            try {
                JIRA.trigger(JIRA.Events.REFRESH_ISSUE_PAGE, [JIRA.Issue.getIssueId()]);
            } catch (e){
                throw new Error("Failed to refresh the issue page");
            }
        }
    };

    rpc.extend(function () {
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
