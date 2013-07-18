package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.remotable.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import hudson.plugins.jira.soap.RemoteAuthenticationException;
import hudson.plugins.jira.soap.RemoteProject;
import it.AbstractBrowserlessTest;
import org.apache.http.client.HttpResponseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;

import static com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;

public class TestJiraNoBrowser extends AbstractBrowserlessTest
{
    private RemoteProject project;

    private final JiraOps jiraOps;

    public static final String ADMIN = "admin";

    public TestJiraNoBrowser()
    {
        super(JiraTestedProduct.class);
        jiraOps = new JiraOps(baseUrl);
    }

    @Before
    public void setUp() throws RemoteException, RemoteAuthenticationException
    {
        project = jiraOps.createProject();

    }

    @After
    public void tearDown() throws RemoteException
    {
        jiraOps.deleteProject(project.getKey());
    }

    @Test(expected = HttpResponseException.class)
    public void testSearchRequestViewPageWithQuoteInUrl() throws Exception
    {
        new AtlassianConnectAddOnRunner(baseUrl,
                "quoteUrl")
                .addSearchRequestView("page", "Hello", "/page\"", newMustacheServlet("hello-world-page.mu"))
                .start();
    }
}
