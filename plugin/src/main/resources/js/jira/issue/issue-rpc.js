alert('ssieu rpc');
(function($, require){
    "use strict";
    require(["ac/jira/issue", "connect-host"], function(jiraIssue, _AP){
        _AP.extend(function () {
            return {
                internals: {
                    openCreateIssueDialog: function (callback, fields) {
                        alert("A");
                        jiraIssue.createIssueDialog(callback, fields);
                    }
                }
            };
        });
    });

})(AJS.$, require);
