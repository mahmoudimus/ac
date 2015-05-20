package it.jira;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;

import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Locale;

import static com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider.getJiraTestedProduct;
import static com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider.getTestedProduct;
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

    public TestJiraWebHooks() {}

    @Test
    public void testWebHookOnIssueCreated() throws Exception
    {
        runInJsonRunner(baseUrl, AddonTestUtils.randomAddOnKey(), "jira:issue_created", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String projectKey = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
                getJiraTestedProduct().backdoor().project().addProject("Test project " + projectKey, projectKey, "admin");
                getJiraTestedProduct().backdoor().issues().createIssue(projectKey, "As Filip I want JIRA WebHooks to really work.");
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
                String projectKey = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
                getJiraTestedProduct().backdoor().project().addProject("Test project " + projectKey, projectKey, "admin");
                getJiraTestedProduct().backdoor().issues().createIssue(projectKey, "As Filip I really like creating issues.");
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
                String projectKey = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
                getJiraTestedProduct().backdoor().project().addProject("Test project " + projectKey, projectKey, "admin");
                IssueCreateResponse issue = getJiraTestedProduct().backdoor().issues().createIssue(projectKey, "As Ben I want JIRA WebHooks listeners to get issue updates");
                getJiraTestedProduct().backdoor().issues().setSummary(issue.key, "As Ben I want JIRA WebHooks listeners to get all issue updates");
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
                String projectKey = RandomStringUtils.randomAlphabetic(4).toUpperCase(Locale.US);
                getJiraTestedProduct().backdoor().project().addProject("Test project " + projectKey, projectKey, "admin");
                IssueCreateResponse issue = getJiraTestedProduct().backdoor().issues().createIssue(projectKey, "As Ben I want JIRA WebHooks listeners to get issue transition");
                int transitionId = getJiraTestedProduct().backdoor().issues().getIssue(issue.key, Issue.Expand.transitions).transitions.get(0).id;
                getJiraTestedProduct().backdoor().issues().transitionIssue(issue.key, transitionId);
                WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertEquals("jira:issue_updated", body.find("webhookEvent"));
                assertThat(body.find("issue"), Matchers.containsString("As Ben I want"));
            }
        });
    }

}
