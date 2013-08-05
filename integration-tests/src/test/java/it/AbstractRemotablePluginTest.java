package it;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.remotable.test.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.Rule;

public abstract class AbstractRemotablePluginTest
{
    protected final static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;

    @Rule
    public final HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    @After
    public final void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }
}
