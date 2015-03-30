package it.jira;


import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import com.atlassian.fugue.Option;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import hudson.plugins.jira.soap.RemoteProject;
import it.util.TestUser;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;

public class JiraWebDriverTestBase
{
    protected static JiraOps jiraOps;
    protected static RemoteProject project;
    protected static JiraTestedProduct product = TestedProductProvider.getJiraTestedProduct();

    protected static ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());
    private static Option<TestUser> currentUser = none();

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
        currentUser = Option.<TestUser>none();
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    protected void login(TestUser user)
    {
        if (isAlreadyLoggedIn(user))
        {
            return;
        }

        logout();
        currentUser = some(user);
        connectPageOperations.dismissAnyAlerts(); // we've seen an alert pop up after the @Before has run

        product.quickLogin(user.getUsername(), user.getPassword());
    }

    protected <T> T loginAndRun(TestUser user, Callable<T> test) throws Exception
    {
        logout();
        login(user);
        try {
            return test.call();
        }
        finally
        {
            logout();
        }
    }

    private boolean isAlreadyLoggedIn(final TestUser user)
    {
        return user != null && currentUser.isDefined() && currentUser.get().getUsername().equals(user.getUsername());
    }

    protected <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args)
    {
        if (isAlreadyLoggedIn(user))
        {
            connectPageOperations.dismissAnyAlerts();
            return product.visit(page, args);
        }

        logout();
        currentUser = some(user);
        connectPageOperations.dismissAnyAlerts(); // we've seen an alert at this point

        return product.quickLogin(user.getUsername(), user.getPassword(), page, args);
    }
}
