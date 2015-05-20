package it.common.iframe;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.AccessDeniedIFramePage;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.PluginManagerPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import it.servlet.condition.CheckUsernameConditionServlet;
import it.util.TestUser;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.TimeZone;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestGeneralPageCrossProduct extends MultiProductWebDriverTestBase
{
    public static final String PAGE_NAME = "A";
    public static final String AMD_PAGE_NAME = "AMD";
    public static final String BETTY_PAGE_NAME = "Betty";
    public static final String ENCODED_SPACES_PAGE_NAME = "Enc";
    public static final String SIZE_TO_PARENT_GENERAL_PAGE = "SzP";
    private static ConnectRunner remotePlugin;

    private static TestUser betty;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        betty = testUserFactory.admin();
        final String productContextPath = product.getProductInstance().getContextPath().toLowerCase();
        String globallyVisibleLocation = productContextPath.contains("jira")
                ? "system.top.navigation.bar"
                : productContextPath.contains("wiki") || productContextPath.contains("confluence")
                ? "system.header/left"
                : null;

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addJWT()
                .addModules("generalPages",
                        newPageBean()
                                .withKey("remotePluginGeneral")
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withUrl("/rpg")
                                .withLocation(globallyVisibleLocation)
                                .withIcon(newIconBean()
                                        .withUrl("/public/sandcastles.jpg")
                                        .build())
                                .build(),
                        newPageBean()
                                .withKey("amdTest")
                                .withName(new I18nProperty(AMD_PAGE_NAME, null))
                                .withUrl("/amdTest")
                                .withLocation(globallyVisibleLocation)
                                .build(),
                        newPageBean()
                                .withKey("onlyBetty")
                                .withName(new I18nProperty(BETTY_PAGE_NAME, null))
                                .withUrl("/ob")
                                .withLocation(globallyVisibleLocation)
                                .withConditions(
                                        newSingleConditionBean()
                                                .withCondition("user_is_logged_in")
                                                .build(),
                                        newSingleConditionBean()
                                                .withCondition("/only" + betty.getDisplayName() + "Condition")
                                                .build())
                                .build(),
                        newPageBean()
                                .withKey("encodedSpaces")
                                .withName(new I18nProperty(ENCODED_SPACES_PAGE_NAME, null))
                                .withUrl("/my?bologne=O%20S%20C%20A%20R")
                                .withLocation(globallyVisibleLocation)
                                .build(),
                        newPageBean()
                                .withKey("sizeToParent")
                                .withName(new I18nProperty(SIZE_TO_PARENT_GENERAL_PAGE, null))
                                .withUrl("/fsg")
                                .withLocation(globallyVisibleLocation)
                                .build())
                .addRoute("/rpg", ConnectAppServlets.apRequestServlet())
                .addRoute("/amdTest", ConnectAppServlets.amdTestServlet())
                .addRoute("/ob", ConnectAppServlets.apRequestServlet())
                .addRoute("/only" + betty.getDisplayName() + "Condition", new CheckUsernameConditionServlet(betty))
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
        loginAndVisit(betty, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "remotePluginGeneral", PAGE_NAME, remotePlugin.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();
        assertTrue(remotePluginTest.getTitle().contains("Remotable Plugin app1 General"));
        assertEquals("Success", remotePluginTest.getMessage());
        assertTrue(remotePluginTest.getIframeQueryParams().containsKey("cp"));
        assertNotNull(remotePluginTest.getFullName());
        assertThat(remotePluginTest.getFullName().toLowerCase(), Matchers.containsString(betty.getUsername()));
        assertEquals(betty.getUsername(), remotePluginTest.getUserId());
        assertTrue(remotePluginTest.getLocale().startsWith("en-"));

        // timezone should be the same as the default one
        assertEquals(TimeZone.getDefault().getRawOffset(), TimeZone.getTimeZone(remotePluginTest.getTimeZone()).getRawOffset());

        // basic tests of the RA.request API
        assertEquals("200", remotePluginTest.getClientHttpStatus());
        String statusText = remotePluginTest.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText));
        String contentType = remotePluginTest.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain"));
        assertEquals(betty.getUsername(), remotePluginTest.getClientHttpData());
        assertEquals(betty.getUsername(), remotePluginTest.getClientHttpResponseText());

        // media type tests of the RA.request API
        assertEquals("{\"name\": \"" + betty.getUsername() + "\"}", remotePluginTest.getClientHttpDataJson());
        assertEquals("<user><name>" + betty.getUsername() + "</name></user>", remotePluginTest.getClientHttpDataXml());
    }

    @Test
    public void testNoAdminPageForNonAdmin()
    {
        login(testUserFactory.basicUser());
        AccessDeniedIFramePage page = product.getPageBinder().bind(AccessDeniedIFramePage.class, "app1", "remotePluginAdmin");
        assertFalse(page.isIframeAvailable());
    }

    @Test
    public void testRemoteConditionSucceeds()
    {
        loginAndVisit(betty, HomePage.class);
        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, "onlyBetty", BETTY_PAGE_NAME, remotePlugin.getAddon().getKey());
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
            login(testUserFactory.admin());
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
        loginAndVisit(testUserFactory.admin(), HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "encodedSpaces", ENCODED_SPACES_PAGE_NAME, remotePlugin.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertThat(remotePluginTest.getValueBySelector("#hello-world-message"), is("Hello world"));
    }

    @Test
    public void testAmd()
    {
        loginAndVisit(testUserFactory.admin(), HomePage.class);

        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "amdTest", AMD_PAGE_NAME, remotePlugin.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertEquals("true", remotePluginTest.waitForValue("amd-env"));
        assertEquals("true", remotePluginTest.waitForValue("amd-request"));
        assertEquals("true", remotePluginTest.waitForValue("amd-dialog"));
    }

    @Test
    public void testSizeToParent()
    {
        loginAndVisit(testUserFactory.admin(), HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "sizeToParent", SIZE_TO_PARENT_GENERAL_PAGE, remotePlugin.getAddon().getKey());
        assertTrue(page.isRemotePluginLinkPresent());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertTrue(remotePluginTest.isFullSize());
    }
}
