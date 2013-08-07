package it.jira;

import java.rmi.RemoteException;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.SearchRequestViewModule;

import org.apache.http.client.HttpResponseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import hudson.plugins.jira.soap.RemoteAuthenticationException;
import hudson.plugins.jira.soap.RemoteProject;
import it.AbstractBrowserlessTest;

import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;

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
        new AtlassianConnectAddOnRunner(baseUrl, "quoteUrl")
                .add(SearchRequestViewModule.key("page")
                                            .name("Hello")
                                            .path("/page\"")
                                            .resource(newMustacheServlet("hello-world-page.mu")))
                .start();
    }
}
