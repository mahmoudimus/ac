(function(){
    define(['ac/jira/events'], function(jiraEvent) {

        module("JIRA events", {
            setup: function() {
                window.JIRA = {
                    trigger: sinon.spy(),
                    Events: {
                        REFRESH_ISSUE_PAGE: sinon.spy()
                    },
                    Issue: {
                        getIssueId: sinon.stub().returns("abc-123")
                    }
                };
            },
            teardown: function() {
                window.JIRA = undefined;
            }
        });

        test("refreshIssuePage triggers a refresh issue page event", function() {
            jiraEvent.refreshIssuePage();
            ok(window.JIRA.trigger.calledOnce);
        });


        test("refreshIssuePage passes a REFERSH_ISSUE_PAGE event", function() {
            jiraEvent.refreshIssuePage();
            deepEqual(window.JIRA.trigger.args[0][0], window.JIRA.Events.REFRESH_ISSUE_PAGE);
        });

        test("refreshIssuePage passes the issue id", function() {
            jiraEvent.refreshIssuePage();
            equal(window.JIRA.trigger.args[0][1], "abc-123");
        });

    });
})();
