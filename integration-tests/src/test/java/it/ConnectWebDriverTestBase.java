package it;

import com.atlassian.jira.testkit.client.PluginsControl;
import com.atlassian.jira.testkit.client.util.TestKitLocalEnvironmentData;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import static it.TestConstants.ADMIN_USERNAME;
import static it.TestConstants.BARNEY_USERNAME;
import static it.TestConstants.BETTY_USERNAME;

public abstract class ConnectWebDriverTestBase
{
    protected static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;

    @Rule
    public WebDriverScreenshotRule screenshotRule = new WebDriverScreenshotRule();

//    @Inject
    protected ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());

    @BeforeClass
    public static void setup()
    {
        // disable license banner
        if("jira".equalsIgnoreCase(product.getProductInstance().getInstanceId()))
        {
            new PluginsControl(new TestKitLocalEnvironmentData()).disablePluginModule("com.atlassian.support.stp:stp-license-status-resources");
        }
    }

    @Before
    @After
    public final void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    protected HomePage loginAsAdmin()
    {
        return loginAs(ADMIN_USERNAME, ADMIN_USERNAME);
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
