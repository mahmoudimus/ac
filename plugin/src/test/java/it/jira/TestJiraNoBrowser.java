package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.remotable.test.RemotePluginRunner;
import com.atlassian.plugin.remotable.test.jira.JiraOps;
import hudson.plugins.jira.soap.RemoteAuthenticationException;
import hudson.plugins.jira.soap.RemoteProject;
import it.AbstractBrowserlessTest;
import org.apache.http.client.HttpResponseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;

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
        new RemotePluginRunner(baseUrl,
                "quoteUrl")
                .addSearchRequestView("page", "Hello", "/page\"", "hello-world-page.mu")
                .start();
    }
}
