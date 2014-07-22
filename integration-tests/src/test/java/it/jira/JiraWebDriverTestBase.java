package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import hudson.plugins.jira.soap.RemoteProject;
import it.ConnectWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

public class JiraWebDriverTestBase extends ConnectWebDriverTestBase
{
    protected static JiraOps jiraOps;
    protected static RemoteProject project;

    @BeforeClass
    public static void beforeClass() throws RemoteException
    {
        jiraOps = new JiraOps(product.getProductInstance());
        project = jiraOps.createProject();
    }

    @AfterClass
    public static void afterClass() throws RemoteException
    {
        jiraOps.deleteProject(project.getKey());
    }

    protected void testLoggedInAndAnonymous(Callable runnable) throws Exception
    {
        getProduct().quickLoginAsAdmin();
        runnable.call();
        logout();
        runnable.call();
    }

    protected static JiraTestedProduct getProduct()
    {
        return (JiraTestedProduct) product;
    }

}
