package it.capabilities.jira;


import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewIssuePageWithRemotePluginIssueTab;
import com.atlassian.plugin.connect.test.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.webhook.WebHookTestServlet;
import com.atlassian.plugin.connect.test.webhook.WebHookTester;
import com.atlassian.plugin.connect.test.webhook.WebHookWaiter;
import com.google.common.collect.ImmutableMap;
import hudson.plugins.jira.soap.RemoteIssue;
import hudson.plugins.jira.soap.RemoteNamedObject;
import hudson.plugins.jira.soap.RemoteProject;
import it.AbstractBrowserlessTest;
import it.jira.JiraWebDriverTestBase;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;

import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInJsonRunner;
import static com.atlassian.plugin.connect.test.webhook.WebHookTestServlet.runInRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestJiraWebHooks extends AbstractBrowserlessTest
{
    private final JiraOps jiraOps;

    public TestJiraWebHooks() {
        super(JiraTestedProduct.class);
        this.jiraOps = new JiraOps(baseUrl);
    }

    @Test
    public void testWebHookOnIssueCreated() throws Exception
    {
        runInJsonRunner(baseUrl, "issue_created", "jira:issue_created", new WebHookTester() {
            @Override
            public void test(WebHookWaiter waiter) throws Exception {
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
        runInJsonRunner(baseUrl, "issue_updated", "jira:issue_updated", new WebHookTester() {
            @Override
            public void test(WebHookWaiter waiter) throws Exception {
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

    @Test
    public void testWebHookOnIssueTransitioned() throws Exception
    {
        runInJsonRunner(baseUrl, "issue_transitioned", "jira:issue_updated", new WebHookTester() {
            @Override
            public void test(WebHookWaiter waiter) throws Exception {
                RemoteProject project = jiraOps.createProject();
                RemoteIssue issue = jiraOps.createIssue(project.getKey(), "As Ben I want JIRA WebHooks listeners to get issue transition");
                RemoteNamedObject[] availableActions = jiraOps.availableActions(issue.getKey());
                jiraOps.transitionIssue(issue.getKey(), availableActions[0].getId(), ImmutableMap.of("summary", "As Ben I want JIRA WebHooks listeners to get all issue transitions"));
                WebHookBody body = waiter.waitForHook();
                assertNotNull(body);
                assertEquals("jira:issue_updated", body.find("webhookEvent"));
                assertThat(body.find("issue"), Matchers.containsString("As Ben I want"));
            }
        });
    }

}
