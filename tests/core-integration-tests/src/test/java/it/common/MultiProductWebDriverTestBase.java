package it.common;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectPageOperations;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.atlassian.webdriver.testing.rule.LogPageSourceRule;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;

import com.atlassian.plugin.connect.test.product.TestedProductAccessor;
import com.atlassian.plugin.connect.test.common.util.ConnectTestUserFactory;
import com.atlassian.plugin.connect.test.common.util.TestUser;

public abstract class MultiProductWebDriverTestBase
{
    protected static ConnectTestUserFactory testUserFactory;
    private static TestedProductAccessor testedProductAccessor;
    protected static TestedProduct<WebDriverTester> product;

    @Rule
    public WebDriverScreenshotRule screenshotRule = new WebDriverScreenshotRule();

    @Rule
    public LogPageSourceRule pageSourceRule = new LogPageSourceRule();

    protected static ConnectPageOperations connectPageOperations()
    {
        return new ConnectPageOperations(product.getPageBinder(), product.getTester().getDriver());
    }

    @BeforeClass
    public static void createTestUserFactory()
    {
        testedProductAccessor = TestedProductAccessor.get();
        product = testedProductAccessor.getTestedProduct();
        testUserFactory = testedProductAccessor.getUserFactory();
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
        testedProductAccessor.login(user);
    }

    protected <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args)
    {
        logout();
        return testedProductAccessor.loginAndVisit(user, page, args);
    }

    protected static String getGloballyVisibleLocation()
    {
        return testedProductAccessor.getGloballyVisibleLocation();
    }
}
