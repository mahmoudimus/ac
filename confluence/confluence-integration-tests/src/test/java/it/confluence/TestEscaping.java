package it.confluence;

import java.net.MalformedURLException;

import com.atlassian.confluence.pageobjects.page.admin.ConfluenceAdminHomePage;
import com.atlassian.confluence.pageobjects.page.content.CreatePage;
import com.atlassian.confluence.pageobjects.page.space.ViewSpaceSummaryPage;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceAdminPage;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceUserProfilePage;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceViewPage;
import com.atlassian.connect.test.confluence.pageobjects.ConnectConfluenceAdminHomePage;
import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.plugin.connect.test.utils.IframeUtils;

import com.google.common.base.Optional;

import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.servlet.ConnectAppServlets;
import redstone.xmlrpc.XmlRpcFault;

import static com.atlassian.connect.test.confluence.pageobjects.ConfluenceTestedProductAccessor.toConfluenceUser;
import static it.confluence.ConfluenceWebDriverTestBase.TestSpace.DEMO;
import static org.junit.Assert.assertEquals;

public class TestEscaping extends ConfluenceWebDriverTestBase
{
    private static final String MODULE_NAME = "F1ND M3 <b>${user}</b>";
    private static final String MODULE_NAME_CONF_ESCAPED = "F1ND M3 <b>\\${user}</b>";
    private static final String MACRO_EDITOR_TITLE = "Insert ‘" + MODULE_NAME_CONF_ESCAPED + "’ Macro";

    private static final String GENERAL_PAGE_KEY = "general-page";
    private static final String WEB_ITEM_KEY = "web-item";
    private static final String ADMIN_PAGE_KEY = "admin-page";
    private static final String MACRO_KEY = "macro";
    private static final String PROFILE_PAGE_KEY = "profile-page";
    private static final String SPACE_TOOLS_TAB_KEY = "space-tools-tab";

    private static final String MODULE_URL = "/page";

    private static final Logger logger = LoggerFactory.getLogger(TestEscaping.class);

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModule("generalPages",
                        ConnectPageModuleBean.newPageBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(GENERAL_PAGE_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModule("webItems",
                        WebItemModuleBean.newWebItemBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(WEB_ITEM_KEY)
                                .withUrl(MODULE_URL)
                                .withContext(AddOnUrlContext.addon)
                                .withLocation("system.content.action")
                                .withWeight(1)
                                .withTooltip(new I18nProperty(MODULE_NAME, null))
                                .build()
                )
                .addModule("adminPages",
                        ConnectPageModuleBean.newPageBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(ADMIN_PAGE_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModule("dynamicContentMacros",
                        DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(MACRO_KEY)
                                .withUrl(MODULE_URL)
                                .withDescription(new I18nProperty(MODULE_NAME, null))
                                .withParameters(MacroParameterBean.newMacroParameterBean()
                                        .withName(new I18nProperty(MODULE_NAME, null))
                                        .withDescription(new I18nProperty(MODULE_NAME, null))
                                        .withIdentifier("test")
                                        .build())
                                .build()
                )
                .addModule("profilePages",
                        ConnectPageModuleBean.newPageBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(PROFILE_PAGE_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModules("spaceToolsTabs", SpaceToolsTabModuleBean.newSpaceToolsTabBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(SPACE_TOOLS_TAB_KEY)
                                .withLocation("overview")
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addRoute(MODULE_URL, ConnectAppServlets.helloWorldServlet())
                .start();

    }

    protected CreatePage editorPage = null;

    // clean up so that we don't get "org.openqa.selenium.UnhandledAlertException: unexpected alert open" in tests
    @After
    public void afterEachTest()
    {
        if (null != editorPage)
        {
            try
            {
                editorPage.cancel();
                editorPage = null;
            }
            catch (Throwable t)
            {
                logger.error("Failed to cancel editor page due to the following Throwable. This will most likely result in 'unexpected alert open' exceptions in subsequent tests.", t);
            }
        }
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void testGeneralPage() throws Exception
    {
        ConnectConfluenceAdminHomePage adminHomePage = loginAndVisit(testUserFactory.admin(), ConnectConfluenceAdminHomePage.class);
        adminHomePage.openHelpMenu();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(GENERAL_PAGE_KEY), Optional.<String>absent());
        assertIsEscaped(webItem.getLinkText());
    }

    @Test
    public void testWebItem() throws Exception
    {
        login(testUserFactory.basicUser());
        createAndVisitViewPage();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(WEB_ITEM_KEY), Optional.of("action-menu-link"));
        webItem.hover();
        assertIsEscaped(webItem.getTitle());
        assertIsEscaped(webItem.getLinkText());
    }

    @Test
    public void testAdminPage() throws Exception
    {
        loginAndVisit(testUserFactory.admin(), ConfluenceAdminHomePage.class);
        ConfluenceAdminPage adminPage = product.getPageBinder().bind(ConfluenceAdminPage.class, runner.getAddon().getKey(), ADMIN_PAGE_KEY);
        assertIsEscaped(adminPage.getRemotePluginLinkText());
    }

    @Test
    public void testMacroTitle() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(toConfluenceUser(testUserFactory.basicUser()), DEMO);

        final MacroBrowserAndEditor macroBrowserAndEditor = findMacroInBrowser(editorPage, "F1ND M3");

        try
        {
            Assert.assertNotNull(macroBrowserAndEditor.macro);
            assertIsEscaped(macroBrowserAndEditor.macro.getTitle().byDefaultTimeout());
        }
        finally
        {
            macroBrowserAndEditor.browserDialog.clickCancel();
        }
    }

    @Test
    public void testMacroEditorTitle() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(toConfluenceUser(testUserFactory.basicUser()), DEMO);

        final MacroBrowserAndEditor macroBrowserAndEditor = selectMacro(editorPage, "F1ND M3");

        try
        {
            Assert.assertNotNull(macroBrowserAndEditor.macroForm);
            assertEquals(MACRO_EDITOR_TITLE, macroBrowserAndEditor.macroForm.getTitle().byDefaultTimeout());
        }
        finally
        {
            macroBrowserAndEditor.browserDialog.clickCancel();
        }
    }

    @Test
    public void testMacroParameter() throws Exception
    {
        editorPage = getProduct().loginAndCreatePage(toConfluenceUser(testUserFactory.basicUser()), DEMO);

        final MacroBrowserAndEditor macroBrowserAndEditor = selectMacro(editorPage, "F1ND M3");

        try
        {
            Assert.assertNotNull(macroBrowserAndEditor.macroForm);
            Assert.assertTrue(macroBrowserAndEditor.macroForm.getField("test").isVisible());
            WebElement label = connectPageOperations.findLabel("macro-param-test");
            assertIsEscaped(label.getText());
        }
        finally
        {
            macroBrowserAndEditor.browserDialog.clickCancel();
        }
    }

    @Test
    public void testProfilePage() throws Exception
    {
        loginAndVisit(testUserFactory.basicUser(), ConfluenceUserProfilePage.class);
        RemoteWebItem webItem = connectPageOperations.findWebItem(RemoteWebItem.ItemMatchingMode.JQUERY,
                "a[href*='" + getServletPath(PROFILE_PAGE_KEY) + "']", Optional.<String>absent());
        assertIsEscaped(webItem.getLinkText());
    }

    @Test
    public void testSpaceAdminTab() throws Exception
    {
        loginAndVisit(testUserFactory.admin(), ViewSpaceSummaryPage.class, TestSpace.DEMO);
        LinkedRemoteContent addonPage = connectPageOperations.findRemoteLinkedContent(
                RemoteWebItem.ItemMatchingMode.LINK_TEXT, MODULE_NAME, Option.<String>none(), getModuleKey(SPACE_TOOLS_TAB_KEY));
        assertIsEscaped(addonPage.getWebItem().getLinkText());
    }

    private void assertIsEscaped(String text)
    {
        // Confluence's own escaping leaves a '\' in front of the '$', which seems wrong, so checking both flavours
        // Note that we're checking against the original name, not an escaped version, as getText() returns the
        // unescaped text. If markup was interpreted, the tags would be missing in the text.
        Assert.assertThat(text, CoreMatchers.anyOf(CoreMatchers.is(MODULE_NAME), CoreMatchers.is(MODULE_NAME_CONF_ESCAPED)));
    }

    private ConfluenceViewPage createAndVisitViewPage() throws Exception
    {
        return createAndVisitPage(ConfluenceViewPage.class);
    }

    private <P extends Page> P createAndVisitPage(Class<P> pageClass) throws Exception
    {
        String pageId = Long.toString(createPage());
        return product.visit(pageClass, pageId);
    }

    private long createPage() throws MalformedURLException, XmlRpcFault
    {
        return rpc.createPage(new com.atlassian.confluence.it.Page(TestSpace.DEMO, RandomStringUtils.randomAlphabetic(8), "some page content"));
    }

    private String getModuleKey(String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(runner.getAddon().getKey(), module);
    }

    private String getServletPath(String module)
    {
        return "/confluence" + IframeUtils.iframeServletPath(runner.getAddon().getKey(), module);
    }
}
