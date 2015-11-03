(function($, define){
    "use strict";
    define("ac/jira/issue", ['jquery'], function($){
        var contextReady;

        function createIssueDialog(callback, fields){
            requireResources().then(function() {
                if(!JIRA || !JIRA.Forms || !JIRA.Forms.createCreateIssueForm){
                    if(console && console.warn){
                        console.warn("Connect: Create issue form is not available");
                    }
                    return false;
                }
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
            }.bind(this));
        }

        function requireResources() {
            var result = new $.Deferred();

            getContextReady().then(function() {
                WRM.require(['wr!com.atlassian.jira.jira-quick-edit-plugin:quick-create-issue']).then(result.resolve.bind(result));
            });

            return result;
        }

        function getContextReady() {
            if (!contextReady) {
                contextReady = new $.Deferred();

                WRM.curl(['wrm/builder', "wrm/resource-base-url-pattern"], function (Builder, resourceBaseUrlPattern) {
                    var contexts = new Builder(resourceBaseUrlPattern).initialize(document).contexts;
                    if (contexts.indexOf('atl.general') > 0) {
                        contextReady.resolve();
                    } else {
                        contextReady.reject();
                    }
                });
            }

            return contextReady;
        }

        return {
            createIssueDialog: createIssueDialog
        };
    });

})(AJS.$, define);
