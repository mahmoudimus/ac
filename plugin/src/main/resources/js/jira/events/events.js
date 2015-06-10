(function($, define){
    "use strict";
    define("ac/jira/events", function(){
        return {
            refreshIssuePage: function(){
                try {
                    JIRA.trigger(JIRA.Events.REFRESH_ISSUE_PAGE, [JIRA.Issue.getIssueId()]);
                } catch (e){
                    throw new Error("Failed to refresh the issue page");
                }
            },
            createIssueDialog: function(callback){
                // yeah, i know.
                JIRA.Forms.createCreateIssueForm({}).asDialog({
                    trigger: document.createElement("a"),
                    id: "create-issue-dialog",
                    windowTitle: AJS.I18n.getText('admin.issue.operations.create')
                }).show();
            }
        };
    });

})(AJS.$, define);
