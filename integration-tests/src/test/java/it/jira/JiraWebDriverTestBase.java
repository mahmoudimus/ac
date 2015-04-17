package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.AddPermissionPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowTransitionPage;
import com.atlassian.jira.tests.TestBase;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import com.atlassian.plugin.connect.test.pageobjects.jira.workflow.ExtendedViewWorkflowTransitionPage;
import com.atlassian.webdriver.testing.rule.LogPageSourceRule;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import hudson.plugins.jira.soap.RemoteProject;
import it.util.ConnectTestUserFactory;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

public class JiraWebDriverTestBase
{
    protected static JiraOps jiraOps;
    protected static RemoteProject project;
    protected static JiraTestedProduct product = TestedProductProvider.getJiraTestedProduct();

    protected static ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());

    private static final int DEFAULT_PERMISSION_SCHEMA = 0;
    private static final int JIRA_PERMISSION_BROWSE_PROJECTS = 10;
    private static final String JIRA_GROUP_ANYONE = "";

    @Rule
    public WebDriverScreenshotRule screenshotRule = new WebDriverScreenshotRule();

    @Rule
    public LogPageSourceRule pageSourceRule = new LogPageSourceRule();

    @BeforeClass
    public static void beforeClass() throws RemoteException
    {
        product.backdoor().restoreBlankInstance();
        
        final AddPermissionPage addPermissionPage = product.quickLoginAsAdmin(AddPermissionPage.class,
                DEFAULT_PERMISSION_SCHEMA, JIRA_PERMISSION_BROWSE_PROJECTS);
        addPermissionPage.setGroup(JIRA_GROUP_ANYONE);
        addPermissionPage.add();

        product.getPageBinder().override(ViewWorkflowTransitionPage.class, ExtendedViewWorkflowTransitionPage.class);

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
        login(ConnectTestUserFactory.basicUser(product));
        runnable.call();
        logout();
        runnable.call();
    }

    @BeforeClass
    @AfterClass
    public static void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    protected void login(TestUser user)
    {
        logout();
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

    protected <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args)
    {
        logout();
        return product.quickLogin(user.getUsername(), user.getPassword(), page, args);
    }
}
