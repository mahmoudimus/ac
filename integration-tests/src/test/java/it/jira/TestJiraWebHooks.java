package it.jira;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;

import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;
import org.junit.Test;

import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteNamedObject;
import hudson.plugins.jira.soap.RemoteProject;

import static com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider.getJiraTestedProduct;
import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInJsonRunner;
import static it.matcher.ParamMatchers.isVersionNumber;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Though jira-webhooks-plugin is a separate component the following test executes a quick smoke test
 * which verifies if jira webhooks are available for AC plugins.
 */
public class TestJiraWebHooks
{
    private final String baseUrl = getJiraTestedProduct().getProductInstance().getBaseUrl();
    private final JiraOps jiraOps;

    public TestJiraWebHooks()
    {
        this.jiraOps = new JiraOps();
    }

    @Test
    public void testWebHookOnIssueCreated() throws Exception
    {
        runInJsonRunner(baseUrl, AddonTestUtils.randomAddOnKey(), "jira:issue_created", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String projectKey = jiraOps.createProject();
                jiraOps.createIssue(projectKey, "As Filip I want JIRA WebHooks to really work.");
                WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertEquals("jira:issue_created", body.find("webhookEvent"));
                assertThat(body.find("issue"), Matchers.containsString("As Filip I want"));
            }
        });
    }

    @Test
    public void testWebHookHasVersion() throws Exception
    {
        runInJsonRunner(baseUrl, AddonTestUtils.randomAddOnKey(), "jira:issue_created", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String projectKey = jiraOps.createProject();
                jiraOps.createIssue(projectKey, "As Filip I really like creating issues.");
                WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertThat(body.getConnectVersion(),isVersionNumber());
            }
        });
    }

    @Test
    public void testWebHookOnIssueUpdated() throws Exception
    {
        runInJsonRunner(baseUrl, AddonTestUtils.randomAddOnKey(), "jira:issue_updated", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String projectKey = jiraOps.createProject();
                IssueCreateResponse issue = jiraOps.createIssue(projectKey, "As Ben I want JIRA WebHooks listeners to get issue updates");
                jiraOps.setIssueSummary(issue.key, "As Ben I want JIRA WebHooks listeners to get all issue updates");
                WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertEquals("jira:issue_updated", body.find("webhookEvent"));
                assertThat(body.find("issue"), Matchers.containsString("As Ben I want"));
            }
        });
    }

    @Test
    public void testWebHookOnIssueTransitioned() throws Exception
    {
        runInJsonRunner(baseUrl, AddonTestUtils.randomAddOnKey(), "jira:issue_updated", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String projectKey = jiraOps.createProject();
                IssueCreateResponse issue = jiraOps.createIssue(projectKey, "As Ben I want JIRA WebHooks listeners to get issue transition");
                //RemoteNamedObject[] availableActions = jiraOps.availableActions(issueKey);
                jiraOps.transitionIssue(issue.key);
                WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertEquals("jira:issue_updated", body.find("webhookEvent"));
                assertThat(body.find("issue"), Matchers.containsString("As Ben I want"));
            }
        });
    }

}
