package it.jira;


import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import com.google.common.collect.ImmutableMap;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteNamedObject;
import hudson.plugins.jira.soap.RemoteProject;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider.getJiraTestedProduct;
import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInJsonRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestJiraWebHooks2
{
    private final String baseUrl = getJiraTestedProduct().getProductInstance().getBaseUrl();
    public static final String JIRA_ISSUE_CREATED = "jira:issue_created";
    public static final String JIRA_ISSUE_UPDATED = "jira:issue_updated";
    private final JiraOps jiraOps;

    public TestJiraWebHooks2()
    {
        this.jiraOps = new JiraOps(baseUrl);
    }

    @Test
    public void testWebHookOnIssueCreated() throws Exception
    {
        runInJsonRunner(baseUrl, "issue_created", JIRA_ISSUE_CREATED, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                RemoteProject project = jiraOps.createProject();
                jiraOps.createIssue(project.getKey(), "As Filip I want JIRA WebHooks to really work.");
                WebHookBody body = waiter.waitForHook();
                assertWebHookDidFire(body, JIRA_ISSUE_CREATED);
            }
        });
    }

    private void assertWebHookDidFire(WebHookBody body, String event) throws Exception
    {
        assertNotNull(body);
        assertEquals(event, body.find("webhookEvent"));
        assertThat(body.find("issue"), Matchers.containsString("As Filip I want"));
    }

    @Test
    public void testWebHookOnIssueUpdated() throws Exception
    {
        runInJsonRunner(baseUrl, "issue_updated", JIRA_ISSUE_UPDATED, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                RemoteProject project = jiraOps.createProject();
                RemoteIssue issue = jiraOps.createIssue(project.getKey(), "As Filip I want JIRA WebHooks listeners to get issue updates");
                jiraOps.updateIssue(issue.getKey(), ImmutableMap.of("summary", "As Filip I want JIRA WebHooks listeners to get all issue updates"));
                WebHookBody body = waiter.waitForHook();
                assertWebHookDidFire(body, JIRA_ISSUE_UPDATED);
            }
        });
    }

    @Test
    public void testWebHookOnIssueTransitioned() throws Exception
    {
        runInJsonRunner(baseUrl, "issue_transitioned", "jira:issue_updated", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                RemoteProject project = jiraOps.createProject();
                RemoteIssue issue = jiraOps.createIssue(project.getKey(), "As Filip I want JIRA WebHooks listeners to get issue transition");
                RemoteNamedObject[] availableActions = jiraOps.availableActions(issue.getKey());
                jiraOps.transitionIssue(issue.getKey(), availableActions[0].getId(), ImmutableMap.of("summary", "As Filip I want JIRA WebHooks listeners to get all issue transitions"));
                WebHookBody body = waiter.waitForHook();
                assertWebHookDidFire(body, JIRA_ISSUE_UPDATED);
            }
        });
    }

}
