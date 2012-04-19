package it;

import com.atlassian.labs.remoteapps.test.*;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestPageModules extends AbstractRemoteAppTest
{
    @Test
	public void testMyGeneralLoaded()
	{
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemoteAppAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remoteAppGeneral",
                                                               "Remote App app1 General");
        assertTrue(page.isRemoteAppLinkPresent());
        RemoteAppTestPage remoteAppTest = page.clickRemoteAppLink();
        assertEquals("Success", remoteAppTest.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), remoteAppTest.getConsumerKey());
        assertEquals("Betty Admin", remoteAppTest.getFullName());
	}

    @Test
    public void testLoadGeneralDialog()
    {
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemoteAppAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remoteAppDialog",
                "Remote App app1 Dialog");
        assertTrue(page.isRemoteAppLinkPresent());
        RemoteAppTestPage remoteAppTest = page.clickRemoteAppLink();
        assertEquals("Betty Admin", remoteAppTest.getFullName());

        // Exercise the dialog's submit button.
        RemoteAppDialog dialog = product.getPageBinder().bind(RemoteAppDialog.class, remoteAppTest);
        assertFalse(dialog.wasSubmitted());
        assertEquals(false, dialog.submit());
        assertTrue(dialog.wasSubmitted());
        assertEquals(true, dialog.submit());
    }

    @Test
    public void testNoAdminPageForNonAdmin()
    {
        product.visit(LoginPage.class).login("barney", "barney", AdminHomePage.class);
        AccessDeniedIFramePage page = product.getPageBinder().bind(AccessDeniedIFramePage.class,
                "app1", "remoteAppAdmin");
        assertFalse(page.isIframeAvailable());
    }

    @Test
    public void testConfigurePage() throws Exception
    {
        RemoteAppRunner runner = new RemoteAppRunner(product.getProductInstance().getBaseUrl(),
                "configurePage")
                .addConfigurePage("page", "Page", "/page", "hello-world-page.mu")
                .start();

        long loadTime = product.visit(LoginPage.class).login("betty", "betty",
                PluginManagerPage.class)
            .configurePlugin("configurePage", "page", RemoteAppTestPage.class)
            .getLoadTime();

        assertTrue(loadTime > 0);

        runner.stop();
    }
}
