package it;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.LicenseStatusBannerHelper;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.atlassian.webdriver.testing.rule.LogPageSourceRule;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import it.util.TestUser;
import org.apache.http.auth.AuthenticationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.io.IOException;

public abstract class ConnectWebDriverTestBase
{
    protected static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;

    protected static String currentUsername = null;

    @Rule
    public WebDriverScreenshotRule screenshotRule = new WebDriverScreenshotRule();

    @Rule
    public LogPageSourceRule pageSourceRule = new LogPageSourceRule();

    /*
    @Rule
    public JavaScriptErrorsRule logConsoleOutputRule = new JavaScriptErrorsRule();
    */

    protected static ConnectPageOperations connectPageOperations = new ConnectPageOperations(product.getPageBinder(),
            product.getTester().getDriver());

    @BeforeClass
    public static void disableLicenseBanner() throws IOException, AuthenticationException
    {
        // disable license banner
        LicenseStatusBannerHelper.instance().execute(product);
    }

    @Before
    @After
    public void dismissPrompts()
    {
        // dismiss any alerts, because they would stop the logout
        connectPageOperations.dismissAnyAlerts();
        connectPageOperations.dismissAnyAuiDialog();
        connectPageOperations.dismissClosableAuiMessage();

        if (product instanceof ConfluenceTestedProduct)
        {
            connectPageOperations.dismissConfluenceDiscardDraftsPrompt();
        }
    }

    @BeforeClass
    @AfterClass
    public static void logout()
    {
        currentUsername = null;
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    protected void login(TestUser user)
    {
        if (!isAlreadyLoggedIn(user))
        {
            logout();
            currentUsername = user.getUsername();
            connectPageOperations.dismissAnyAlerts(); // we've seen an alert pop up after the @Before has run

            if (product instanceof JiraTestedProduct)
            {
                JiraTestedProduct jiraTestedProduct = (JiraTestedProduct) product;
                jiraTestedProduct.quickLogin(user.getUsername(), user.getPassword());
            }
            else
            {
                product.visit(LoginPage.class).login(user.getUsername(), user.getPassword(), HomePage.class);
            }
        }
    }

    private boolean isAlreadyLoggedIn(final TestUser user)
    {
        return user != null && user.getUsername().equals(currentUsername);
    }

    protected <P extends Page> P loginAndVisit(TestUser user, final Class<P> page, final Object... args)
    {
        if (isAlreadyLoggedIn(user))
        {
            connectPageOperations.dismissAnyAlerts();
            return product.visit(page, args);
        }

        logout();
        currentUsername = user.getUsername();
        connectPageOperations.dismissAnyAlerts(); // we've seen an alert at this point

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

    protected String getModuleKey(ConnectRunner runner, String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(runner.getAddon().getKey(), module);
    }

    protected String getModuleKey(String addonKey, String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(addonKey, module);
    }
}
