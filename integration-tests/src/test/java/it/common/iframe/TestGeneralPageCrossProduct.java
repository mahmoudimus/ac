package it.common.iframe;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.*;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import it.util.TestUser;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.TimeZone;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static java.lang.String.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TestGeneralPageCrossProduct extends MultiProductWebDriverTestBase
{
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        final String productContextPath = product.getProductInstance().getContextPath().toLowerCase();
        String globallyVisibleLocation = productContextPath.contains("jira")
            ? "system.top.navigation.bar"
            : productContextPath.contains("wiki") || productContextPath.contains("confluence")
                ? "system.help/pages"
                : null;

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addJWT()
                .addModules("generalPages",
                        newPageBean()
                                .withKey("remotePluginGeneral")
                                .withName(new I18nProperty("Remotable Plugin app1 General", null))
                                .withUrl("/rpg")
                                .withLocation(globallyVisibleLocation)
                                .withIcon(newIconBean()
                                        .withUrl("/public/sandcastles.jpg")
                                        .build())
                                .build(),
                        newPageBean()
                                .withKey("amdTest")
                                .withName(new I18nProperty("AMD Test app1 General", null))
                                .withUrl("/amdTest")
                                .withLocation(globallyVisibleLocation)
                                .build(),
                        newPageBean()
                                .withKey("onlyBetty")
                                .withName(new I18nProperty("Only Betty", null))
                                .withUrl("/ob")
                                .withLocation(globallyVisibleLocation)
                                .withConditions(
                                        newSingleConditionBean()
                                                .withCondition("user_is_logged_in")
                                                .build(),
                                        newSingleConditionBean()
                                                .withCondition("/onlyBettyCondition")
                                                .build())
                                .build(),
                        newPageBean()
                                .withKey("encodedSpaces")
                                .withName(new I18nProperty("Encoded Spaces", null))
                                .withUrl("/my?bologne=O%20S%20C%20A%20R")
                                .withLocation(globallyVisibleLocation)
                                .build(),
                        newPageBean()
                                .withKey("sizeToParent")
                                .withName(new I18nProperty("Size to parent general page", null))
                                .withUrl("/fsg")
                                .withLocation(globallyVisibleLocation)
                                .build())
                .addRoute("/rpg", ConnectAppServlets.apRequestServlet())
                .addRoute("/amdTest", ConnectAppServlets.amdTestServlet())
                .addRoute("/ob", ConnectAppServlets.apRequestServlet())
                .addRoute("/onlyBettyCondition", new OnlyBettyConditionServlet())
                .addRoute("/my", ConnectAppServlets.helloWorldServlet())
                .addRoute("/fsg", ConnectAppServlets.sizeToParentServlet())
                .addScope(ScopeName.READ)
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testMyGeneralLoaded()
    {
        login(TestUser.BETTY);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneral", "Remotable Plugin app1 General", remotePlugin.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        assertTrue(remotePluginTest.getTitle().contains("Remotable Plugin app1 General"));
        assertEquals("Success", remotePluginTest.getMessage());
        assertTrue(remotePluginTest.getIframeQueryParams().containsKey("cp"));
        assertNotNull(remotePluginTest.getFullName());
        assertThat(remotePluginTest.getFullName().toLowerCase(), Matchers.containsString(TestUser.BETTY.getUsername()));
        assertEquals(TestUser.BETTY.getUsername(), remotePluginTest.getUserId());
        assertTrue(remotePluginTest.getLocale().startsWith("en-"));

        // timezone should be the same as the default one
        assertEquals(TimeZone.getDefault().getRawOffset(), TimeZone.getTimeZone(remotePluginTest.getTimeZone()).getRawOffset());

        // basic tests of the RA.request API
        assertEquals("200", remotePluginTest.getClientHttpStatus());
        String statusText = remotePluginTest.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText));
        String contentType = remotePluginTest.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain"));
        assertEquals(TestUser.BETTY.getUsername(), remotePluginTest.getClientHttpData());
        assertEquals(TestUser.BETTY.getUsername(), remotePluginTest.getClientHttpResponseText());

        // media type tests of the RA.request API
        assertEquals("{\"name\": \"betty\"}", remotePluginTest.getClientHttpDataJson());
        assertEquals("<user><name>betty</name></user>", remotePluginTest.getClientHttpDataXml());
    }

    @Test
    public void testNoAdminPageForNonAdmin()
    {
        login(TestUser.BARNEY);
        AccessDeniedIFramePage page = product.getPageBinder().bind(AccessDeniedIFramePage.class, "app1", "remotePluginAdmin");
        assertFalse(page.isIframeAvailable());
    }

    @Test
    public void testRemoteConditionSucceeds()
    {
        login(TestUser.BETTY);

        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, "onlyBetty", "Only Betty", remotePlugin.getAddon().getKey());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertTrue(remotePluginTest.getTitle().contains("Only Betty"));
    }

    @Test
    public void testConfigurePage() throws Exception
    {
        ConnectRunner anotherPlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, new InstallHandlerServlet())
                .addModule("configurePage", newPageBean()
                        .withName(new I18nProperty("Page", null))
                        .withKey("page")
                        .withLocation("")
                        .withUrl("/page")
                        .build())
                .addRoute("/page", ConnectAppServlets.helloWorldServlet())
                .addScope(ScopeName.READ)
                .start();

        try
        {
            login(TestUser.BETTY);
            final PluginManagerPage upm = product.visit(PluginManagerPage.class);

            upm.clickConfigurePluginButton(anotherPlugin.getAddon().getKey(), "page");
            product.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, anotherPlugin.getAddon().getKey(), "page", true); // will throw if it fails to load
        }
        finally
        {
            anotherPlugin.stopAndUninstall();
        }
    }

    @Test
    public void testEncodedSpaceInPageModuleUrl()
    {
        // Regression test for AC-885 (ensure descriptor query strings are not decoded before parsing)
        loginAndVisit(TestUser.BETTY, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "encodedSpaces", "Encoded Spaces", remotePlugin.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertThat(remotePluginTest.getValueBySelector("#hello-world-message"), is("Hello world"));
    }

    @Test
    public void testAmd()
    {
        loginAndVisit(TestUser.BETTY, HomePage.class);

        String LINK_TEXT = "AMD Test app1 General";
//        RemoteWebItem webItem = connectPageOperations.findWebItem(RemoteWebItem.ItemMatchingMode.LINK_TEXT, LINK_TEXT, Optional.<String>absent());

        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "amdTest", LINK_TEXT, remotePlugin.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertEquals("true", remotePluginTest.waitForValue("amd-env"));
        assertEquals("true", remotePluginTest.waitForValue("amd-request"));
        assertEquals("true", remotePluginTest.waitForValue("amd-dialog"));
    }

    @Test
    public void testSizeToParent()
    {
        loginAndVisit(TestUser.BETTY, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "sizeToParent", "Size to parent general page", remotePlugin.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertTrue(remotePluginTest.isFullSize());
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