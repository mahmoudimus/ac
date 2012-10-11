package it;

import com.atlassian.plugin.remotable.test.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.OwnerOfTestedProduct;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.MethodRule;

public class AbstractRemotablePluginTest
{
    protected static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;
    @Rule
    public MethodRule rule = new HtmlDumpRule(product.getTester().getDriver());

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }
}
