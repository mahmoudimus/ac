(function($, define){
    "use strict";
    define("ac/jira/issue", function(){
        return {
            createIssueDialog: function(callback, fields){

                var dialog = JIRA.Forms.createCreateIssueForm(fields).asDialog({
                    trigger: document.createElement("a"),
                    id: "create-issue-dialog",
                    windowTitle: AJS.I18n.getText('admin.issue.operations.create')
                });

                dialog.show();
                var sanitizedIssues = [];
                var createCallback = function (e, issues) {
                    var i, sanitizedIssue;
                    sanitizedIssues = [];
                    for(i in issues) {
                        sanitizedIssues.push(issues[i].createdIssueDetails);
                    }
                };

                JIRA.one("QuickCreateIssue.sessionComplete", createCallback);
                dialog.bind('Dialog.hide', function(){
                    if($.isFunction(callback)){
                        callback.call({}, sanitizedIssues);
                    }
                });
            }
        };
    });

})(AJS.$, define);
