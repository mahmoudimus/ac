package it.jira;


import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import hudson.plugins.jira.soap.RemoteProject;
import it.util.TestUser;

public class JiraWebDriverTestBase
{
    protected static JiraOps jiraOps;
    protected static RemoteProject project;
    protected static JiraTestedProduct product = TestedProductProvider.getJiraTestedProduct();
    protected static String currentUsername = null;

    protected static ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());

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
        product.quickLoginAsAdmin();
        runnable.call();
        logout();
        runnable.call();
    }

    @BeforeClass
    @AfterClass
    public static void logout()
    {
        currentUsername = null;
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    protected void login(TestUser user)
    {
        if (isAlreadyLoggedIn(user))
        {
            return;
        }

        logout();
        currentUsername = user.getUsername();
        connectPageOperations.dismissAnyAlerts(); // we've seen an alert pop up after the @Before has run

        product.quickLogin(user.getUsername(), user.getPassword());
    }

    private boolean isAlreadyLoggedIn(final TestUser user)
    {
        return user != null && user.getUsername().equals(currentUsername);
    }

    protected <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args)
    {
        if (isAlreadyLoggedIn(user))
        {
            connectPageOperations.dismissAnyAlerts();
            return product.visit(page, args);
        }

        logout();
        currentUsername = user.getUsername();
        connectPageOperations.dismissAnyAlerts(); // we've seen an alert at this point

        return product.quickLogin(user.getUsername(), user.getPassword(), page, args);
    }
}
