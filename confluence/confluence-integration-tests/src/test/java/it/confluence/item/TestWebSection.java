package it.confluence.item;

import com.atlassian.confluence.pageobjects.component.menu.ConfluenceMenuItem;
import com.atlassian.confluence.pageobjects.component.menu.ToolsMenu;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.confluence.ConfluenceWebDriverTestBase;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;

public class TestWebSection extends ConfluenceWebDriverTestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();

    private static final String TOOLS_LOCATION = "system.content.action";

    // The web section in the tools menu
    private static final String WEB_SECTION_ID = "dropdown-section";
    private static final String WEB_SECTION_NAME = "D-D-Drop";

    // The web item within the dropdown
    private static final String CONTENT_WEB_ITEM_ID = "dropdown-item";
    private static final String CONTENT_WEB_ITEM_NAME = "Much Bass";
    private static final String CONTENT_WEB_ITEM_URL = "/da.bass";
    private static final String CONTENT_LOCATION = TOOLS_LOCATION + "/" + ModuleKeyUtils.addonAndModuleKey(PLUGIN_KEY, WEB_SECTION_ID);

    private static ConnectRunner addon;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(
                        "webItems",
                        WebItemModuleBean.newWebItemBean()
                                .withName(new I18nProperty(CONTENT_WEB_ITEM_NAME, null))
                                .withKey(CONTENT_WEB_ITEM_ID)
                                .withUrl(CONTENT_WEB_ITEM_URL)
                                .withLocation(CONTENT_LOCATION)
                                .build()
                )
                .addModule(
                        "webSections",
                        WebSectionModuleBean.newWebSectionBean()
                                .withName(new I18nProperty(WEB_SECTION_NAME, null))
                                .withLocation(TOOLS_LOCATION)
                                .withKey(WEB_SECTION_ID)
                                .build()
                )
                .start();
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
        TestUser user = testUserFactory.basicUser();
        final ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(Option.some(user), "ds", "Page with web section", "some page content");
        final String pageId = pageData.getId();

        ViewPage viewPage = loginAndVisit(user, ViewPage.class, pageId);

        ToolsMenu toolsMenu = viewPage.openToolsMenu();

        ConfluenceMenuItem webItem = toolsMenu.getMenuItem(By.id(ModuleKeyUtils.addonAndModuleKey(PLUGIN_KEY, CONTENT_WEB_ITEM_ID)));
        Assert.assertTrue("Web item within web section should be found", webItem.isVisible());
    }
}
