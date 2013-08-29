package it;

import java.io.IOException;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.OAuthUtils;
import com.atlassian.plugin.connect.test.pageobjects.*;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.Condition;
import com.atlassian.plugin.connect.test.server.module.ConfigurePageModule;
import com.atlassian.plugin.connect.test.server.module.DialogPageModule;
import com.atlassian.plugin.connect.test.server.module.GeneralPageModule;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;
import static it.TestConstants.BETTY_USERNAME;
import static java.lang.String.valueOf;
import static org.junit.Assert.*;

public class TestPageModules extends AbstractRemotablePluginTest
{
    private static AtlassianConnectAddOnRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .addPermission("resttest")
                .add(GeneralPageModule.key("remotePluginGeneral")
                                      .name("Remotable Plugin app1 General")
                                      .path("/rpg")
                                      .linkName("Remotable Plugin app1 General Link")
                                      .iconUrl("/public/sandcastles.jpg")
                                      .height("600")
                                      .width("700")
                                      .resource(newMustacheServlet("iframe.mu")))
                .add(GeneralPageModule.key("amdTest")
                                      .name("AMD Test app1 General")
                                      .path("/amdTest")
                                      .resource(newMustacheServlet("amd-test.mu")))
                .add(GeneralPageModule.key("onlyBetty")
                                      .name("Only Betty")
                                      .path("/ob")
                                      .conditions(Condition.name("user_is_logged_in"), Condition.at("/onlyBettyCondition").resource(new OnlyBettyConditionServlet()))
                                      .resource(newMustacheServlet("iframe.mu")))
                .add(DialogPageModule.key("remotePluginDialog")
                                     .name("Remotable Plugin app1 Dialog")
                                     .path("/rpd")
                                     .resource(newMustacheServlet("dialog.mu")))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
        }
    }

    @Test
    public void testMyGeneralLoaded()
    {
        product.visit(LoginPage.class).login(BETTY_USERNAME, BETTY_USERNAME, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneral", "Remotable Plugin app1 General Link");

        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();
        assertTrue(remotePluginTest.getTitle().contains("Remotable Plugin app1 General"));
        assertFalse(remotePluginTest.getTitle().contains("Remotable Plugin app1 General Link"));
        assertEquals("Success", remotePluginTest.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), remotePluginTest.getConsumerKey());
        assertTrue(remotePluginTest.getIframeQueryParams().containsKey("cp"));
        assertNotNull(remotePluginTest.getFullName());
        assertThat(remotePluginTest.getFullName().toLowerCase(), Matchers.containsString(BETTY_USERNAME));
        assertEquals(BETTY_USERNAME, remotePluginTest.getUserId());
        assertTrue(remotePluginTest.getLocale().startsWith("en-"));

        // timezone should be the same as the default one
        assertEquals(TimeZone.getDefault().getRawOffset(), TimeZone.getTimeZone(remotePluginTest.getTimeZone()).getRawOffset());

        // basic tests of the RA.request API
        assertEquals("200", remotePluginTest.getClientHttpStatus());
        String statusText = remotePluginTest.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText));
        String contentType = remotePluginTest.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain"));
        assertEquals(BETTY_USERNAME, remotePluginTest.getClientHttpData());
        assertEquals(BETTY_USERNAME, remotePluginTest.getClientHttpResponseText());

        // media type tests of the RA.request API
        assertEquals("{\"name\": \"betty\"}", remotePluginTest.getClientHttpDataJson());
        assertEquals("<user><name>betty</name></user>", remotePluginTest.getClientHttpDataXml());

        // test unauthorized scope access
        // ACDEV-363: Temporarily disabling scope checking on the client until
        // we figure out our long term strategy with permissions
        // assertEquals("403", remotePluginTest.getClientHttpUnauthorizedCode());
    }

    @Test
    public void testLoadGeneralDialog()
    {
        product.visit(LoginPage.class).login(BETTY_USERNAME, BETTY_USERNAME, HomePage.class);

        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginDialog", "Remotable Plugin app1 Dialog");
        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();

        assertNotNull(remotePluginTest.getFullName());
        assertThat(remotePluginTest.getFullName().toLowerCase(), Matchers.containsString(BETTY_USERNAME));

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
        product.visit(LoginPage.class).login(BETTY_USERNAME, BETTY_USERNAME, HomePage.class);

        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, "onlyBetty", "Only Betty");
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();

        assertTrue(remotePluginTest.getTitle().contains("Only Betty"));
    }

    @Test
    public void testConfigurePage() throws Exception
    {
        ConfigurePageModule configPage = ConfigurePageModule.key("page")
                                                            .name("Page")
                                                            .path("/page")
                                                            .resource(newMustacheServlet("hello-world-page.mu"));
        
        AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "configurePage");
        
                runner.add(configPage);
                runner.start();

        // fixme: jira page objects don't redirect properly to next page
        product.visit(LoginPage.class).login(BETTY_USERNAME, BETTY_USERNAME, HomePage.class);
        final PluginManagerPage upm = product.visit(PluginManagerPage.class);
        
        final RemotePluginTestPage remotePluginTestPage = upm.configurePlugin("configurePage", "page", RemotePluginTestPage.class);
        assertTrue(remotePluginTestPage.isLoaded());

        runner.stop();
    }

    @Test
    public void testAmd()
    {
        product.visit(LoginPage.class).login(BETTY_USERNAME, BETTY_USERNAME, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "amdTest", "AMD Test app1 General");
        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();

        assertEquals("true", remotePluginTest.waitForValue("amd-env"));
        assertEquals("true", remotePluginTest.waitForValue("amd-request"));
        assertEquals("true", remotePluginTest.waitForValue("amd-dialog"));
    }

    public static final class OnlyBettyConditionServlet extends HttpServlet
    {
        private static final String BETTY = "betty";

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            final String loggedInUser = req.getParameter("user_id");
            final boolean isBetty = isBetty(loggedInUser);

            logger.debug("The logged in user is {}betty, their user key is '{}'", isBetty ? "" : "NOT ", loggedInUser);

            final String json = getJson(isBetty);
            logger.debug("Responding with the following json: {}", json);
            sendJson(resp, json);
        }

        private void sendJson(HttpServletResponse resp, String json) throws IOException
        {
            resp.setContentType("application/json");
            resp.getWriter().write(json);
            resp.getWriter().close();
        }

        private String getJson(boolean shouldDisplay)
        {
            return "{\"shouldDisplay\" : " + valueOf(shouldDisplay) + "}";
        }

        private boolean isBetty(String loggedInUser)
        {
            return BETTY.equals(loggedInUser);
        }
    }
}
