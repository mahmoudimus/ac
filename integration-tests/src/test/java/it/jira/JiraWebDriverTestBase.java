package it.jira;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import hudson.plugins.jira.soap.RemoteProject;
import it.ConnectWebDriverTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public class JiraWebDriverTestBase extends ConnectWebDriverTestBase
{
    protected static JiraOps jiraOps;
    protected RemoteProject project;

    @BeforeClass
    public static void setup() throws RemoteException
    {
        jiraOps = new JiraOps(product.getProductInstance());
    }

    @Before
    public void prepare() throws RemoteException
    {
        project = jiraOps.createProject();
    }

    @After
    public void tearDown() throws RemoteException
    {
        jiraOps.deleteProject(project.getKey());
    }

    protected void testLoggedInAndAnonymous(Callable runnable) throws Exception
    {
        loginAsAdmin();
        runnable.call();
        logout();
        runnable.call();
    }

}
