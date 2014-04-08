package it;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.LicenseStatusBannerHelper;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import org.apache.http.auth.AuthenticationException;
import org.junit.*;

import java.io.IOException;

import static it.TestConstants.ADMIN_USERNAME;
import static it.TestConstants.BARNEY_USERNAME;
import static it.TestConstants.BETTY_USERNAME;

public abstract class ConnectWebDriverTestBase
{
    protected static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;
    
    protected static String currentUsername = null;

    @Rule
    public WebDriverScreenshotRule screenshotRule = new WebDriverScreenshotRule();

//    @Inject
    protected ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());

    @BeforeClass
    public static void disableLicenseBanner() throws IOException, AuthenticationException
    {
        // disable license banner
        LicenseStatusBannerHelper.instance().execute(product);
    }

    @BeforeClass
    @AfterClass
    public static void logout()
    {
        currentUsername = null;
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    protected void loginAsAdmin()
    {
        if(!ADMIN_USERNAME.equals(currentUsername))
        {
            loginAs(ADMIN_USERNAME, ADMIN_USERNAME);
            this.currentUsername = ADMIN_USERNAME;
        }
    }

    protected void loginAsBetty()
    {
        if(!BETTY_USERNAME.equals(currentUsername))
        {
            loginAs(BETTY_USERNAME, BETTY_USERNAME);
            this.currentUsername = BETTY_USERNAME;
        }
    }

    protected void loginAsBarney()
    {
        if(!BARNEY_USERNAME.equals(currentUsername))
        {
            loginAs(BARNEY_USERNAME, BARNEY_USERNAME);
            this.currentUsername = BARNEY_USERNAME;
        }
    }

    protected HomePage loginAs(String username, String password)
    {
        logout();
        return product.visit(LoginPage.class).login(username, password, HomePage.class);
    }
}
