package it;

import com.atlassian.plugin.remotable.test.*;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.*;

public class TestPageModules extends AbstractRemotablePluginTest
{
    @Test
	public void testMyGeneralLoaded()
	{
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneral",
                                                               "Remotable Plugin app1 General Link");

        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();
        assertTrue(remotePluginTest.getTitle().contains("Remotable Plugin app1 General"));
        assertFalse(remotePluginTest.getTitle().contains("Remotable Plugin app1 General Link"));
        Assert.assertEquals("Success", remotePluginTest.getMessage());
        Assert.assertEquals(OAuthUtils.getConsumerKey(), remotePluginTest.getConsumerKey());
        assertTrue(remotePluginTest.getIframeQueryParams().containsKey("cp"));
        assertNotNull(remotePluginTest.getFullName());
        assertThat(remotePluginTest.getFullName().toLowerCase(), Matchers.containsString("betty"));
        Assert.assertEquals("betty", remotePluginTest.getUserId());
        assertTrue(remotePluginTest.getLocale().startsWith("en-"));

        // timezone should be the same as the default one
        assertEquals(TimeZone.getDefault().getRawOffset(), TimeZone.getTimeZone(remotePluginTest.getTimeZone()).getRawOffset());

        // basic tests of the HostHttpClient API
        Assert.assertEquals("200", remotePluginTest.getServerHttpStatus());
        String statusText = remotePluginTest.getServerHttpStatusText();
        assertTrue("OK".equals(statusText));
        String contentType = remotePluginTest.getServerHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain"));
        Assert.assertEquals("betty", remotePluginTest.getServerHttpEntity());

        // basic tests of the RA.request API
        Assert.assertEquals("200", remotePluginTest.getClientHttpStatus());
        statusText = remotePluginTest.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText));
        contentType = remotePluginTest.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain"));
        Assert.assertEquals("betty", remotePluginTest.getClientHttpData());
        Assert.assertEquals("betty", remotePluginTest.getClientHttpResponseText());
    }

    @Test
    public void testLoadGeneralDialog()
    {
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginDialog",
                "Remotable Plugin app1 Dialog");
        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();
        assertNotNull(remotePluginTest.getFullName());
        assertThat(remotePluginTest.getFullName().toLowerCase(), Matchers.containsString("betty"));

        // Exercise the dialog's submit button.
        RemotePluginDialog dialog = product.getPageBinder().bind(RemotePluginDialog.class, remotePluginTest);
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
                "app1", "remotePluginAdmin");
        assertFalse(page.isIframeAvailable());
    }

    @Test
    @Ignore("Need to wait for menu to open w/o waiting for page link name")
    public void testRemoteConditionFails()
    {
        product.visit(LoginPage.class).login("barney", "barney", HomePage.class);
        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, "onlyBetty",
                "Only Betty");
        assertFalse(page.isRemotePluginLinkPresent());
    }

    @Test
    public void testRemoteConditionSucceeds()
    {
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, "onlyBetty",
                "Only Betty");
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();
        assertTrue(remotePluginTest.getTitle().contains("Only Betty"));
    }

    @Test
    public void testConfigurePage() throws Exception
    {
        RemotePluginRunner runner = new RemotePluginRunner(product.getProductInstance().getBaseUrl(),
                "configurePage")
                .addConfigurePage("page", "Page", "/page", "hello-world-page.mu")
                .start();

        // fixme: jira page objects don't redirect properly to next page
        product.visit(LoginPage.class).login("betty", "betty",
                HomePage.class);
        assertTrue(product.visit(PluginManagerPage.class).configurePlugin("configurePage", "page", RemotePluginTestPage.class)
            .isLoaded());

        runner.stop();
    }

    @Test
    public void testAmd()
    {
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "amdTest",
            "AMD Test app1 General");
        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();

        Assert.assertEquals("true", remotePluginTest.waitForValue("amd-env"));
        Assert.assertEquals("true", remotePluginTest.waitForValue("amd-request"));
        Assert.assertEquals("true", remotePluginTest.waitForValue("amd-dialog"));
    }
}
