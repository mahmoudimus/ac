package it.jira;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraOps;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import hudson.plugins.jira.soap.RemoteProject;

import static it.TestConstants.ADMIN_USERNAME;

public class JiraWebDriverTestBase
{
    protected static TestedProduct<WebDriverTester> product;
    protected static JiraOps jiraOps;
    protected RemoteProject project;

    @BeforeClass
    public static void setup() throws RemoteException
    {
        product = TestedProductFactory.create(JiraTestedProduct.class);
        jiraOps = new JiraOps(product.getProductInstance());
    }

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
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


    protected void loginAsAdmin()
    {
        loginAs(ADMIN_USERNAME, ADMIN_USERNAME);
    }

    protected void loginAs(String username, String password)
    {
        product.visit(LoginPage.class).login(username, password, DashboardPage.class);
    }
}
