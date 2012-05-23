package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.labs.remoteapps.test.jira.JiraOps;
import com.atlassian.labs.remoteapps.test.webhook.WebHookBody;
import com.atlassian.labs.remoteapps.test.webhook.WebHookTester;
import com.atlassian.labs.remoteapps.test.webhook.WebHookWaiter;
import hudson.plugins.jira.soap.RemoteProject;
import it.AbstractBrowserlessTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.labs.remoteapps.test.webhook.WebHookTestServlet.runInRunner;
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
                assertEquals(ADMIN, body.find("issue/reporterName"));
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
                jiraOps.updateIssueSummary(issueKey, "New Summary");
                WebHookBody body = waiter.waitForHook();
                assertEquals(issueKey, body.find("issue/key"));
                assertEquals("Test issue", body.find("updatedFields/summary/oldValue"));
                assertEquals("New Summary", body.find("updatedFields/summary/newValue"));
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
                assertEquals("admin", body.find("comment/author"));
            }
        });
    }
}
