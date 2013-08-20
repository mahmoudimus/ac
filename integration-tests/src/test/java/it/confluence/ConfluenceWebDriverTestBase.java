package it.confluence;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.junit.HtmlDumpRule;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import static it.TestConstants.BETTY;

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
    public HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    @Before
    @After
    public final void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    protected HomePage loginAsBetty()
    {
        return login(BETTY, BETTY);
    }

    protected HomePage login(String pwd, String username)
    {
        return product.visit(LoginPage.class).login(username, pwd, HomePage.class);
    }
}
