package com.atlassian.labs.remoteapps.smoketest;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.labs.remoteapps.test.*;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.*;
import org.junit.rules.MethodRule;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Smoke tests for a Remote Apps instance that can access the Internet
 */
public class TestRemoteApps
{
    private static final String targetBaseUrl = System.getProperty("remoteapps.targetBaseUrl");
    private static final String adminUsername = System.getProperty("remoteapps.adminUsername");
    private static final String adminPassword = System.getProperty("remoteapps.adminPassword");
    
    private static final String displayUrl = System.getProperty("remoteapps.displayUrl",
            targetBaseUrl + "/download/resources/com.atlassian.labs.remoteapps-plugin:smoke-test");
    private static final RemoteAppInstallerClient installer = new RemoteAppInstallerClient(targetBaseUrl, adminUsername,
            adminPassword);

    private static TestedProduct<WebDriverTester> product;
    static
    {
        product = OwnerOfTestedProduct.INSTANCE;
        if (product instanceof ConfluenceTestedProduct)
        {
            product.getPageBinder().override(LoginPage.class, OnDemandConfluenceLoginPage.class);
            product.getPageBinder().override(HomePage.class, OnDemandConfluenceHomePage.class);
        }
    }
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

        installer.install(displayUrl + "/atlassian-remote-app.xml", "");

    }
    
    @AfterClass
    public static void uninstallApp() throws IOException
    {
        installer.uninstall("remoteapps-smoke-test");
    }

    @Test
    public void testGeneralPage()
    {
        product.visit(LoginPage.class).login(adminUsername, adminPassword, HomePage.class);
        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, "smoke-general-page",
                "General Page (smoke test)");
        assertTrue(page.isRemoteAppLinkPresent());
        RemoteAppTestPage remoteAppTest = page.clickRemoteAppLink();
        assertEquals("Hello General Page", remoteAppTest.getMessage());
    }
}
