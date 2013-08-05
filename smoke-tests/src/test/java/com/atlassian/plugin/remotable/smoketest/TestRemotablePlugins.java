package com.atlassian.plugin.remotable.smoketest;

import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.test.client.AtlassianConnectRestClient;
import com.atlassian.plugin.remotable.test.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.pageobjects.GeneralPage;
import com.atlassian.plugin.remotable.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.plugin.remotable.test.pageobjects.RemotePluginTestPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Smoke tests for a Remotable Plugins instance that can access the Internet
 */
public class TestRemotablePlugins
{
    private static final String adminUsername = System.getProperty("remotable.plugins.adminUsername", "admin");
    private static final String adminPassword = System.getProperty("remotable.plugins.adminPassword", "admin");
    
    private static TestedProduct<WebDriverTester> product;
    static
    {
        product = OwnerOfTestedProduct.INSTANCE;
        if (product instanceof ConfluenceTestedProduct)
        {
            product.getPageBinder().override(HomePage.class, OnDemandConfluenceHomePage.class);
        }
    }

    private static final String targetBaseUrl = System.getProperty("remotable.plugins.targetBaseUrl", product.getProductInstance().getBaseUrl());

    private static final String displayUrl = System.getProperty("remotable.plugins.displayUrl", targetBaseUrl + "/download/resources/com.atlassian.labs.remoteapps-plugin:smoke-test");
    private static final AtlassianConnectRestClient INSTALLER = new AtlassianConnectRestClient(targetBaseUrl, adminUsername, adminPassword);

    @Rule
    public HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

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

        INSTALLER.install(displayUrl + "/atlassian-plugin.xml");

    }
    
    @AfterClass
    public static void uninstallApp() throws IOException
    {
        INSTALLER.uninstall("remotable-plugins-smoke-test");
    }

    @Test
    public void testGeneralPage()
    {
        product.visit(LoginPage.class).login(adminUsername, adminPassword, HomePage.class);
        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, "smoke-general-page", "General Page (smoke test)");
        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();
        assertEquals("Hello General Page", remotePluginTest.getMessage());
    }
}
