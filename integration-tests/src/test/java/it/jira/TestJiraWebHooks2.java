package it.jira;


import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Locale;

import static com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider.getJiraTestedProduct;
import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInJsonRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestJiraWebHooks2
{
    private final String baseUrl = getJiraTestedProduct().getProductInstance().getBaseUrl();
    private static final String JIRA_ISSUE_CREATED = "jira:issue_created";
    private static final String JIRA_ISSUE_UPDATED = "jira:issue_updated";

    public TestJiraWebHooks2() {}

    @Test
    public void testWebHookOnIssueCreated() throws Exception
    {
        runInJsonRunner(baseUrl, "issue_created", JIRA_ISSUE_CREATED, new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String projectKey = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
                getJiraTestedProduct().backdoor().project().addProject("Test project " + projectKey, projectKey, "admin");
                getJiraTestedProduct().backdoor().issues().createIssue(projectKey, "As Filip I want JIRA WebHooks to really work.");
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
                String projectKey = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
                getJiraTestedProduct().backdoor().project().addProject("Test project " + projectKey, projectKey, "admin");
                IssueCreateResponse issue = getJiraTestedProduct().backdoor().issues().createIssue(projectKey, "As Filip I want JIRA WebHooks listeners to get issue updates");
                getJiraTestedProduct().backdoor().issues().setSummary(issue.key, "As Filip I want JIRA WebHooks listeners to get all issue updates");
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
                String projectKey = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
                getJiraTestedProduct().backdoor().project().addProject("Test project " + projectKey, projectKey, "admin");
                IssueCreateResponse issue = getJiraTestedProduct().backdoor().issues().createIssue(projectKey, "As Filip I want JIRA WebHooks listeners to get issue transition");
                int transitionId = getJiraTestedProduct().backdoor().issues().getIssue(issue.key, Issue.Expand.transitions).transitions.get(0).id;
                getJiraTestedProduct().backdoor().issues().transitionIssue(issue.key, transitionId);
                WebHookBody body = waiter.waitForHook();
                assertWebHookDidFire(body, JIRA_ISSUE_UPDATED);
            }
        });
    }

}
