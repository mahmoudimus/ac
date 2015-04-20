package it.common;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.TestedProductProvider;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.atlassian.webdriver.testing.rule.LogPageSourceRule;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import it.util.ConfluenceTestUserFactory;
import it.util.ConnectTestUserFactory;
import it.util.JiraTestUserFactory;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

public abstract class MultiProductWebDriverTestBase
{
    protected static TestedProduct<WebDriverTester> product = TestedProductProvider.getTestedProduct();

    protected static ConnectTestUserFactory testUserFactory;

    @Rule
    public WebDriverScreenshotRule screenshotRule = new WebDriverScreenshotRule();

    @Rule
    public LogPageSourceRule pageSourceRule = new LogPageSourceRule();

    protected static ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());

    @BeforeClass
    public static void createTestUserFactory()
    {
        if (product instanceof JiraTestedProduct)
        {
            testUserFactory = new JiraTestUserFactory((JiraTestedProduct)product);
        }
        else if (product instanceof ConfluenceTestedProduct)
        {
            testUserFactory = new ConfluenceTestUserFactory((ConfluenceTestedProduct)product);
        }
        else
        {
            throw new UnsupportedOperationException("Sorry, I don't know how to log into " + product.getClass().getCanonicalName());
        }
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
        if (product instanceof JiraTestedProduct)
        {
            JiraTestedProduct jiraTestedProduct = (JiraTestedProduct) product;
            jiraTestedProduct.quickLogin(user.getUsername(), user.getPassword());
        }
        else if (product instanceof ConfluenceTestedProduct)
        {
            product.visit(LoginPage.class).login(user.getUsername(), user.getPassword(), HomePage.class);
        }
        else
        {
            throw new UnsupportedOperationException("Sorry, I don't know how to log into " + product.getClass().getCanonicalName());
        }
    }

    protected <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args)
    {
        logout();

        if (product instanceof JiraTestedProduct)
        {
            JiraTestedProduct jiraTestedProduct = (JiraTestedProduct) product;
            return jiraTestedProduct.quickLogin(user.getUsername(), user.getPassword(), page, args);
        }
        else if (product instanceof ConfluenceTestedProduct)
        {
            ConfluenceTestedProduct confluenceTestedProduct = (ConfluenceTestedProduct) product;
            return confluenceTestedProduct.login(user.confUser(), page, args);
        }
        else
        {
            throw new UnsupportedOperationException("Sorry, I don't know how to log into " + product.getClass().getCanonicalName());
        }
    }
}
