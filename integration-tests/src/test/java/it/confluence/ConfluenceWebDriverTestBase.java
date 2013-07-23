package it.confluence;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.remotable.test.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

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
}
