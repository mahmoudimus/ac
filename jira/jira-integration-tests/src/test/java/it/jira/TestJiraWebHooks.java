package it.jira;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.common.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.common.webhook.WebHookWaiter;

import org.hamcrest.Matchers;
import org.junit.Test;

import static com.atlassian.plugin.connect.test.common.matcher.ParamMatchers.isVersionNumber;
import static com.atlassian.plugin.connect.test.common.servlet.WebHookTestServlet.runInJsonRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Though jira-webhooks-plugin is a separate component the following test executes a quick smoke test
 * which verifies if jira webhooks are available for AC plugins.
 */
public class TestJiraWebHooks extends JiraTestBase
{
    private final String baseUrl = product.getProductInstance().getBaseUrl();

    public TestJiraWebHooks() {}

    @Test
    public void testWebHookOnIssueCreated() throws Exception
    {
        runInJsonRunner(baseUrl, AddonTestUtils.randomAddOnKey(), "jira:issue_created", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                product.backdoor().issues().createIssue(project.getKey(), "As Filip I want JIRA WebHooks to really work.");
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
                product.backdoor().issues().createIssue(project.getKey(), "As Filip I really like creating issues.");
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
                IssueCreateResponse issue = product.backdoor().issues().createIssue(project.getKey(), "As Ben I want JIRA WebHooks listeners to get issue updates");
                product.backdoor().issues().setSummary(issue.key, "As Ben I want JIRA WebHooks listeners to get all issue updates");
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
                IssueCreateResponse issue = product.backdoor().issues().createIssue(project.getKey(), "As Ben I want JIRA WebHooks listeners to get issue transition");
                int transitionId = product.backdoor().issues().getIssue(issue.key, Issue.Expand.transitions).transitions.get(0).id;
                product.backdoor().issues().transitionIssue(issue.key, transitionId);
                WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertEquals("jira:issue_updated", body.find("webhookEvent"));
                assertThat(body.find("issue"), Matchers.containsString("As Ben I want"));
            }
        });
    }

}
