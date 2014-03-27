package it.confluence;

import com.atlassian.confluence.pageobjects.component.menu.ConfluenceMenuItem;
import com.atlassian.confluence.pageobjects.component.menu.ToolsMenu;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean.newWebSectionBean;
import static it.TestConstants.ADMIN_USERNAME;
import static org.junit.Assert.assertTrue;

public class TestWebSection extends ConfluenceWebDriverTestBase
{
    private static final String PLUGIN_KEY = RemotePluginUtils.randomPluginKey();

    private static final String TOOLS_LOCATION = "system.content.action";

    // The web section in the tools menu
    private static final String WEB_SECTION_ID = "dropdown-section";
    private static final String WEB_SECTION_NAME = "D-D-Drop";

    // The web item within the dropdown
    private static final String CONTENT_WEB_ITEM_ID = "dropdown-item";
    private static final String CONTENT_WEB_ITEM_NAME = "Much Bass";
    private static final String CONTENT_WEB_ITEM_URL = "/da.bass";
    private static final String CONTENT_LOCATION = TOOLS_LOCATION + "/" + WEB_SECTION_ID;

    private static ConnectRunner addon;
    private static ConfluenceOps.ConfluenceUser admin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(
                        "webItems",
                        newWebItemBean()
                                .withName(new I18nProperty(CONTENT_WEB_ITEM_NAME, null))
                                .withKey(CONTENT_WEB_ITEM_ID)
                                .withUrl(CONTENT_WEB_ITEM_URL)
                                .withLocation(CONTENT_LOCATION)
                                .build()
                )
                .addModule(
                        "webSections",
                        newWebSectionBean()
                                .withName(new I18nProperty(WEB_SECTION_NAME, null))
                                .withLocation(TOOLS_LOCATION)
                                .withKey(WEB_SECTION_ID)
                                .build()
                )
                .start();
        admin = new ConfluenceOps.ConfluenceUser(ADMIN_USERNAME, ADMIN_USERNAME);
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (addon != null)
        {
            addon.stopAndUninstall();
        }
    }

    @Test
    public void testWebItemFoundWithinWebSection() throws MalformedURLException, XmlRpcFault
    {
        final ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(admin), "ds", "Page with web section", "some page content");
        final String pageId = pageData.getId();
        product.visit(LoginPage.class).login(ADMIN_USERNAME, ADMIN_USERNAME, HomePage.class);

        ViewPage viewPage = product.visit(ViewPage.class, pageId);

        ToolsMenu toolsMenu = viewPage.openToolsMenu();

        ConfluenceMenuItem webItem = toolsMenu.getMenuItem(By.id(CONTENT_WEB_ITEM_ID));
        assertTrue("Web item within web section should be found", webItem.isVisible());
    }
}
