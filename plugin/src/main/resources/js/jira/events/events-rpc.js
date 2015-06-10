(function($, require){
    "use strict";
    require(["ac/jira/events", "connect-host"], function(jiraEvents, _AP){
        _AP.extend(function () {
            return {
                init: function (state, xdm){
                    JIRA.bind("QuickCreateIssue.sessionComplete", function (e, issues) {
                        var i,sanitizedIssue, sanitizedIssues = [];
                        for(i in issues) {
                            sanitizedIssues.push(issues[i].createdIssueDetails);
                        }
                        xdm.triggerQuickIssueCreated(sanitizedIssues);
                    });
                },
                internals: {
                    triggerJiraEvent: function () {
                        jiraEvents.refreshIssuePage();
                    },
                    triggerIssueCreate: function () {
                        jiraEvents.createIssueDialog();
                    }
                },
                stubs: ['triggerQuickIssueCreated']
            };
        });
    });

})(AJS.$, require);
