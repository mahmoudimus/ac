package it.confluence;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import static it.TestConstants.*;

public abstract class ConfluenceWebDriverTestBase
{
    static
    {
        System.setProperty("testedProductClass", FixedConfluenceTestedProduct.class.getName());
    }

    protected static TestedProduct<WebDriverTester> product;
    protected static ConfluenceOps confluenceOps;

    @BeforeClass
    public static void setUpConfluence()
    {
        product = OwnerOfTestedProduct.INSTANCE;
        confluenceOps = new ConfluenceOps(product.getProductInstance().getBaseUrl());
    }

    @Rule
    public WebDriverScreenshotRule screenshotRule = new WebDriverScreenshotRule();

    @Before
    @After
    public final void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    protected void loginAsAdmin()
    {
        loginAs(ADMIN_USERNAME, ADMIN_USERNAME);
    }

    protected HomePage loginAsBetty()
    {
        return loginAs(BETTY_USERNAME, BETTY_USERNAME);
    }

    protected HomePage loginAsBarney()
    {
        return loginAs(BARNEY_USERNAME, BARNEY_USERNAME);
    }

    protected HomePage loginAs(String username, String password)
    {
        return product.visit(LoginPage.class).login(username, password, HomePage.class);
    }

}
