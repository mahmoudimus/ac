package it.capabilities.confluence;

import com.atlassian.confluence.pageobjects.page.admin.ConfluenceAdminHomePage;
import com.atlassian.confluence.pageobjects.page.admin.templates.SpaceTemplatesPage;
import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.capabilities.provider.SpaceToolsTabModuleProvider;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceAdminPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceUserProfilePage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConnectConfluenceAdminHomePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.google.common.base.Optional;
import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean.newSpaceToolsTabBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean.newWebPanelBean;
import static com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean.newMacroParameterBean;
import static org.junit.Assert.assertEquals;

public class TestEscaping extends ConfluenceWebDriverTestBase
{
    private static final String MODULE_NAME = "<b>${user}</b>";

    private static final String GENERAL_PAGE_KEY = "general-page";
    private static final String WEB_ITEM_KEY = "web-item";
    private static final String ADMIN_PAGE_KEY = "admin-page";
    private static final String MACRO_KEY = "macro";
    private static final String PROFILE_PAGE_KEY = "profile-page";
    private static final String SPACE_TOOLS_TAB_KEY = "space-tools-tab";

    private static final String MODULE_URL = "/page";

    private static final String SPACE = "ds";

    private static ConnectRunner runner;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), RemotePluginUtils.randomPluginKey())
                .setAuthenticationToNone()
                .addModule("generalPages",
                        newPageBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(GENERAL_PAGE_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModule("webItems",
                        newWebItemBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(WEB_ITEM_KEY)
                                .withUrl(MODULE_URL)
                                .withContext(AddOnUrlContext.addon)
                                .withLocation("system.header/left")
                                .withTooltip(new I18nProperty(MODULE_NAME, null))
                                .build()
                )
                .addModule("adminPages",
                        newPageBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(ADMIN_PAGE_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModule("dynamicContentMacros",
                        newDynamicContentMacroModuleBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(MACRO_KEY)
                                .withUrl(MODULE_URL)
                                .withDescription(new I18nProperty(MODULE_NAME, null))
                                .withParameters(newMacroParameterBean()
                                        .withName(new I18nProperty(MODULE_NAME, null))
                                        .withDescription(new I18nProperty(MODULE_NAME, null))
                                        .withIdentifier("test")
                                        .build())
                                .build()
                )
                .addModule("profilePages",
                        newPageBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(PROFILE_PAGE_KEY)
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addModules("spaceToolsTabs", newSpaceToolsTabBean()
                                .withName(new I18nProperty(MODULE_NAME, null))
                                .withKey(SPACE_TOOLS_TAB_KEY)
                                .withLocation("contenttools")
                                .withUrl(MODULE_URL)
                                .build()
                )
                .addRoute(MODULE_URL, ConnectAppServlets.helloWorldServlet())
                .start();

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
        loginAsAdmin();
        ConnectConfluenceAdminHomePage adminHomePage = product.getPageBinder().bind(ConnectConfluenceAdminHomePage.class);
        adminHomePage.openHelpMenu();
        RemoteWebItem webItem = connectPageOperations.findWebItem(getModuleKey(GENERAL_PAGE_KEY), Optional.<String>absent());
        assertIsEscaped(webItem.getLinkText());
    }

    @Test
    public void testWebItem() throws Exception
    {
        loginAsAdmin();
        RemoteWebItem webItem = findViewPageWebItem(getModuleKey(WEB_ITEM_KEY));
        assertIsEscaped(webItem.getLinkText());
    }

    @Test
    public void testWebItemTooltip() throws Exception
    {
        loginAsAdmin();
        RemoteWebItem webItem = findViewPageWebItem(getModuleKey(WEB_ITEM_KEY));
        assertIsEscaped(webItem.getTitle());
    }

    @Test
    public void testAdminPage() throws Exception
    {
        loginAsAdmin();
        product.visit(ConfluenceAdminHomePage.class);
        ConfluenceAdminPage adminPage = product.getPageBinder().bind(ConfluenceAdminPage.class, getModuleKey(ADMIN_PAGE_KEY));
        assertIsEscaped(adminPage.getRemotePluginLinkText());
    }

    @Test
    public void testMacro() throws Exception
    {

    }

    @Test
    public void testProfilePage() throws Exception
    {
        loginAsAdmin();
        product.visit(ConfluenceUserProfilePage.class);
        RemoteWebItem webItem = connectPageOperations.findWebItem(RemoteWebItem.ItemMatchingMode.JQUERY,
                "a[href*='" + getServletPath(PROFILE_PAGE_KEY) + "']", Optional.<String>absent());
        assertIsEscaped(webItem.getLinkText());
    }

    @Test
    public void testSpaceAdminTab() throws Exception
    {
        loginAsAdmin();
        product.visit(SpaceTemplatesPage.class, "ds");
        LinkedRemoteContent addonPage = connectPageOperations.findTabPanel(
                getModuleKey(SPACE_TOOLS_TAB_KEY) + SpaceToolsTabModuleProvider.SPACE_ADMIN_KEY_SUFFIX,
                Option.<String>none(), getModuleKey(SPACE_TOOLS_TAB_KEY));

        assertIsEscaped(addonPage.getWebItem().getLinkText());
    }

    @Test
    public void testSpaceToolsTab() throws Exception
    {
        loginAsAdmin();
        product.visit(SpaceTemplatesPage.class, "ts");
        RemoteWebItem webItem =  connectPageOperations.findWebItem(RemoteWebItem.ItemMatchingMode.JQUERY,
                "li[data-web-item-key='" + getModuleKey(SPACE_TOOLS_TAB_KEY) + "'] > a", Optional.<String>absent());
        assertIsEscaped(webItem.getLinkText());
    }

    private void assertIsEscaped(String text)
    {
        assertEquals(MODULE_NAME, text);
    }

    private RemoteWebItem findViewPageWebItem(String webItemId) throws Exception
    {
        createAndVisitViewPage();
        return connectPageOperations.findWebItem(webItemId, Optional.<String>absent());
    }

    private ConfluenceViewPage createAndVisitViewPage() throws Exception
    {
        return createAndVisitPage(ConfluenceViewPage.class);
    }


    private <P extends Page> P createAndVisitPage(Class<P> pageClass) throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = createPage();
        return product.visit(pageClass, pageData.getId());
    }

    private ConfluenceOps.ConfluencePageData createPage() throws MalformedURLException, XmlRpcFault
    {
        return confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), SPACE, "Page with webitem", "some page content");
    }

    private String getModuleKey(String module)
    {
        return ModuleKeyUtils.addonAndModuleKey(runner.getAddon().getKey(), module);
    }

    private String getServletPath(String module)
    {
        return "/confluence/plugins/servlet/ac/" + runner.getAddon().getKey() + "/" + module;
    }
}
