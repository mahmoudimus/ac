package com.atlassian.labs.remoteapps.smoketest;

import com.atlassian.labs.remoteapps.test.*;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceGeneralPage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.*;
import org.junit.rules.MethodRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Smoke tests for a Remote Apps instance that can access the Internet
 */
public class TestRemoteApps
{
    private static final String targetBaseUrl = System.getProperty("remoteapps.targetBaseUrl");
    private static final String adminUsername = System.getProperty("remoteapps.adminUsername");
    private static final String adminPassword = System.getProperty("remoteapps.adminPassword");
    
    private static final String displayUrl = System.getProperty("remoteapps.displayUrl",
            "https://bitbucket.org/mrdon/remoteapps-plugin/raw/master/smoke-tests/src/test/app");
    private static final RemoteAppInstallerClient installer = new RemoteAppInstallerClient(targetBaseUrl, adminUsername,
            adminPassword);

    private static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;
    @Rule
    public MethodRule rule = new HtmlDumpRule(product.getTester().getDriver());

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @BeforeClass
    public static void installApp() throws IOException
    {
        assertNotNull(targetBaseUrl);
        assertTrue(targetBaseUrl.length() > 0);
        assertNotNull(adminUsername);
        assertNotNull(adminPassword);
        assertNotNull(displayUrl);

        installer.install(displayUrl + "/atlassian-remote-app.xml");

    }
    
    @AfterClass
    public static void uninstallApp() throws IOException
    {
        installer.uninstall("remoteapps-smoke-test");
    }

    @Test
    public void testAdminPage()
    {
        product.visit(LoginPage.class).login(adminUsername, adminPassword, HomePage.class);
        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, "smoke-admin-page",
                "General Page (smoke test)");
        assertTrue(page.isRemoteAppLinkPresent());
        RemoteAppTestPage remoteAppTest = page.clickRemoteAppLink();
        assertEquals("Hello General Page", remoteAppTest.getMessage());
    }
}
