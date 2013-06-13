package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.remotable.test.jira.JiraOps;
import com.atlassian.plugin.remotable.test.webhook.WebHookBody;
import com.atlassian.plugin.remotable.test.webhook.WebHookTester;
import com.atlassian.plugin.remotable.test.webhook.WebHookWaiter;
import com.google.common.collect.ImmutableMap;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteProject;
import it.AbstractBrowserlessTest;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.atlassian.plugin.remotable.test.webhook.WebHookTestServlet.runInRunner;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Though jira-webhooks-plugin is a seperate component the following test executes a quick smoke test
 * which verifies if jira webhooks are available for AC plugins.
 */
public class SmokeTestJiraWebHooks extends AbstractBrowserlessTest
{

    private final JiraOps jiraOps;

    public SmokeTestJiraWebHooks()
    {
        super(JiraTestedProduct.class);
        this.jiraOps = new JiraOps(baseUrl);
    }

    @Test
    public void testWebHookOnIssueCreated() throws Exception
    {
        runInRunner(baseUrl, "issue_created", "jira:issue_created", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                RemoteProject project = jiraOps.createProject();
                jiraOps.createIssue(project.getKey(), "As Filip I want JIRA WebHooks to really work.");
                WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertEquals("jira:issue_created", body.find("webhookEvent"));
                assertThat(body.find("issue"), Matchers.containsString("As Filip I want"));
            }
        });
    }

    @Test
    public void testWebHookOnIssueUpdated() throws Exception
    {
        runInRunner(baseUrl, "issue_updated", "jira:issue_updated", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                RemoteProject project = jiraOps.createProject();
                RemoteIssue issue = jiraOps.createIssue(project.getKey(), "As Ben I want JIRA WebHooks listeners to get issue updates");
                jiraOps.updateIssue(issue.getKey(), ImmutableMap.of("summary", "As Ben I want JIRA WebHooks listeners to get all issue updates"));
                WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertEquals("jira:issue_updated", body.find("webhookEvent"));
                assertThat(body.find("issue"), Matchers.containsString("As Ben I want"));
            }
        });
    }

}
