package it.jira;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.test.jira.JiraOps;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import hudson.plugins.jira.soap.RemoteProject;
import org.junit.After;
import org.junit.BeforeClass;

import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import static it.TestConstants.ADMIN;

public class JiraWebDriverTestBase
{
    protected static TestedProduct<WebDriverTester> product;
    protected static JiraOps jiraOps;
    protected static RemoteProject project;

    @BeforeClass
    public static void setup() throws RemoteException
    {
        product = TestedProductFactory.create(JiraTestedProduct.class);
        jiraOps = new JiraOps(product.getProductInstance());
        project = jiraOps.createProject();
    }

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
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
        loginAs(ADMIN, ADMIN);
    }

    private void loginAs(String username, String password)
    {
        product.visit(LoginPage.class).login(username, password, DashboardPage.class);
    }
}
