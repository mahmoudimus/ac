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
                                                               "Remote App app1 General Link");
        assertTrue(page.isRemoteAppLinkPresent());
        RemoteAppTestPage remoteAppTest = page.clickRemoteAppLink();
        assertTrue(remoteAppTest.getTitle().contains("Remote App app1 General"));
        assertFalse(remoteAppTest.getTitle().contains("Remote App app1 General Link"));
        assertEquals("Success", remoteAppTest.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), remoteAppTest.getConsumerKey());
        assertEquals("Betty Admin", remoteAppTest.getFullName());
        assertEquals("betty", remoteAppTest.getUserId());

        // basic tests of the HostHttpClient API
        assertEquals("200", remoteAppTest.getServerHttpStatus());
        String statusText = remoteAppTest.getServerHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText)); // differs by jquery version
        String contentType = remoteAppTest.getServerHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain")); // startsWith accounts for possible encoding
        assertEquals("betty", remoteAppTest.getServerHttpEntity());

        // basic tests of the RA.request API
        assertEquals("200", remoteAppTest.getClientHttpStatus());
        statusText = remoteAppTest.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText)); // differs by jquery version
        contentType = remoteAppTest.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain")); // startsWith accounts for possible encoding
        assertEquals("betty", remoteAppTest.getClientHttpData());
        assertEquals("betty", remoteAppTest.getClientHttpResponseText());
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
