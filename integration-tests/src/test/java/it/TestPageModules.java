package it;

import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.test.AccessDeniedIFramePage;
import com.atlassian.plugin.remotable.test.GeneralPage;
import com.atlassian.plugin.remotable.test.OAuthUtils;
import com.atlassian.plugin.remotable.test.PluginManagerPage;
import com.atlassian.plugin.remotable.test.RemotePluginAwarePage;
import com.atlassian.plugin.remotable.test.RemotePluginDialog;
import com.atlassian.plugin.remotable.test.RemotePluginRunner;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import java.util.TimeZone;

import static it.TestConstants.BETTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestPageModules extends AbstractRemotablePluginTest
{
    @Test
    public void testMyGeneralLoaded()
    {
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneral", "Remotable Plugin app1 General Link");

        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();
        assertTrue(remotePluginTest.getTitle().contains("Remotable Plugin app1 General"));
        assertFalse(remotePluginTest.getTitle().contains("Remotable Plugin app1 General Link"));
        assertEquals("Success", remotePluginTest.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), remotePluginTest.getConsumerKey());
        assertTrue(remotePluginTest.getIframeQueryParams().containsKey("cp"));
        assertNotNull(remotePluginTest.getFullName());
        assertThat(remotePluginTest.getFullName().toLowerCase(), Matchers.containsString(BETTY));
        assertEquals(BETTY, remotePluginTest.getUserId());
        assertTrue(remotePluginTest.getLocale().startsWith("en-"));

        // timezone should be the same as the default one
        assertEquals(TimeZone.getDefault().getRawOffset(), TimeZone.getTimeZone(remotePluginTest.getTimeZone()).getRawOffset());

        // basic tests of the HostHttpClient API
        assertEquals("200", remotePluginTest.getServerHttpStatus());
        String statusText = remotePluginTest.getServerHttpStatusText();
        assertTrue("OK".equals(statusText));
        String contentType = remotePluginTest.getServerHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain"));
        assertEquals(BETTY, remotePluginTest.getServerHttpEntity());

        // basic tests of the RA.request API
        assertEquals("200", remotePluginTest.getClientHttpStatus());
        statusText = remotePluginTest.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText));
        contentType = remotePluginTest.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain"));
        assertEquals(BETTY, remotePluginTest.getClientHttpData());
        assertEquals(BETTY, remotePluginTest.getClientHttpResponseText());

        // media type tests of the RA.request API
        //assertEquals("{\"name\": \"betty\"}", remotePluginTest.getClientHttpDataJson());
        //assertEquals("<user><name>betty</name></user>", remotePluginTest.getClientHttpDataXml());
    }

    @Test
    public void testLoadGeneralDialog()
    {
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);

        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginDialog", "Remotable Plugin app1 Dialog");
        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();

        assertNotNull(remotePluginTest.getFullName());
        assertThat(remotePluginTest.getFullName().toLowerCase(), Matchers.containsString(BETTY));

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
        AccessDeniedIFramePage page = product.getPageBinder().bind(AccessDeniedIFramePage.class, "app1", "remotePluginAdmin");
        assertFalse(page.isIframeAvailable());
    }

    @Test
    @Ignore("Need to wait for menu to open w/o waiting for page link name")
    public void testRemoteConditionFails()
    {
        product.visit(LoginPage.class).login("barney", "barney", HomePage.class);
        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, "onlyBetty", "Only Betty");
        assertFalse(page.isRemotePluginLinkPresent());
    }

    @Test
    public void testRemoteConditionSucceeds()
    {
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);

        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, "onlyBetty", "Only Betty");
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
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        final RemotePluginTestPage remotePluginTestPage =
                product.visit(PluginManagerPage.class).configurePlugin("configurePage", "page", RemotePluginTestPage.class);
        assertTrue(remotePluginTestPage.isLoaded());

        runner.stop();
    }

    @Test
    public void testAmd()
    {
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "amdTest", "AMD Test app1 General");
        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();

        assertEquals("true", remotePluginTest.waitForValue("amd-env"));
        assertEquals("true", remotePluginTest.waitForValue("amd-request"));
        assertEquals("true", remotePluginTest.waitForValue("amd-dialog"));
    }
}
