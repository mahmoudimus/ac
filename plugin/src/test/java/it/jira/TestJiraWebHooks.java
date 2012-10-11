package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.remotable.test.jira.JiraOps;
import com.atlassian.plugin.remotable.test.webhook.WebHookBody;
import com.atlassian.plugin.remotable.test.webhook.WebHookTester;
import com.atlassian.plugin.remotable.test.webhook.WebHookWaiter;
import com.google.common.collect.ImmutableMap;
import hudson.plugins.jira.soap.RemoteProject;
import it.AbstractBrowserlessTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.plugin.remotable.test.webhook.WebHookTestServlet.runInRunner;
import static junit.framework.Assert.assertEquals;

public class TestJiraWebHooks extends AbstractBrowserlessTest
{
    private final JiraOps jiraOps;

    public static final String ADMIN = "admin";

    public TestJiraWebHooks()
    {
        super(JiraTestedProduct.class);
        jiraOps = new JiraOps(baseUrl);
    }
    private RemoteProject project;

    @Before
    public void setUp() throws RemoteException
    {
        project = jiraOps.createProject();
    }
    
    @After
    public void tearDown() throws RemoteException
    {
        jiraOps.deleteProject(project.getKey());
    }

    @Test
    public void testIssueCreatedWebHookFired() throws Exception
    {
        runInRunner(baseUrl, "issue_created", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String issueKey = jiraOps.createIssue(project.getKey(), "Test issue").getKey();
                WebHookBody body = waiter.waitForHook();
                assertEquals(issueKey, body.find("issue/key"));
                assertEquals(ADMIN, body.find("issue/fields/reporter/name"));
            }
        });
    }

    @Test
    public void testIssueUpdateWebHookFired() throws Exception
    {
        runInRunner(baseUrl, "issue_updated", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String issueKey = jiraOps.createIssue(project.getKey(), "Test issue").getKey();
                jiraOps.updateIssue(issueKey, ImmutableMap.of(
                        "summary", "New Summary",
                        "description", "foo"));
                WebHookBody body = waiter.waitForHook();
                assertEquals(issueKey, body.find("issue/key"));
                assertEquals("summary", body.find("updatedFields[0]/name"));
                assertEquals("Test issue", body.find("updatedFields[0]/oldValue"));
                assertEquals("New Summary", body.find("updatedFields[0]/newValue"));
            }
        });
    }

    @Test
    public void testIssueCommentedWebHookFired() throws Exception
    {
        runInRunner(baseUrl, "issue_commented", new WebHookTester()
        {
            @Override
            public void test(WebHookWaiter waiter) throws Exception
            {
                String issueKey = jiraOps.createIssue(project.getKey(), "Test issue").getKey();
                jiraOps.addComment(issueKey, "My comment");
                WebHookBody body = waiter.waitForHook();
                assertEquals(issueKey, body.find("issue/key"));
                assertEquals("My comment", body.find("comment/body"));
                assertEquals("admin", body.find("comment/author/name"));
            }
        });
    }
}
