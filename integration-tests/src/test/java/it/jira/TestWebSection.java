package it.jira;

import com.atlassian.jira.pageobjects.components.menu.JiraAuiDropdownMenu;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.RemotePluginUtils;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraViewProjectPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean.newWebSectionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestWebSection extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = RemotePluginUtils.randomPluginKey();

    private static final String JIRA_HEADER_LOCATION = "system.top.navigation.bar";

    // The web item in the header
    private static final String HEADER_WEB_ITEM_ID = "my-dropdown";
    private static final String HEADER_WEB_ITEM_NAME = "The Drop";
    private static final String HEADER_WEB_ITEM_URL = "/le-drop";

    // The web section that makes the dropdown
    private static final String WEB_SECTION_ID = "dropdown-section";
    private static final String WEB_SECTION_NAME = "D-D-Drop";
    private static final String DROPDOWN_LOCATION = addonAndModuleKey(PLUGIN_KEY,HEADER_WEB_ITEM_ID) + "/" + addonAndModuleKey(PLUGIN_KEY,WEB_SECTION_ID);
    private static final String DROPDOWN_CONTENT_ID = HEADER_WEB_ITEM_ID + "-content";

    // The web item within the dropdown
    private static final String CONTENT_WEB_ITEM_ID = "dropdown-item";
    private static final String CONTENT_WEB_ITEM_NAME = "Much Bass";
    private static final String CONTENT_WEB_ITEM_URL = "/da.bass";

    private static ConnectRunner addon;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModules(
                        "webItems",
                        newWebItemBean()
                                .withName(new I18nProperty(HEADER_WEB_ITEM_NAME, null))
                                .withKey(HEADER_WEB_ITEM_ID)
                                .withUrl(HEADER_WEB_ITEM_URL)
                                .withLocation(JIRA_HEADER_LOCATION)
                                .build(),
                        newWebItemBean()
                                .withName(new I18nProperty(CONTENT_WEB_ITEM_NAME, null))
                                .withKey(CONTENT_WEB_ITEM_ID)
                                .withUrl(CONTENT_WEB_ITEM_URL)
                                .withLocation(DROPDOWN_LOCATION)
                                .build()
                        )
                .addModule(
                        "webSections",
                        newWebSectionBean()
                            .withName(new I18nProperty(WEB_SECTION_NAME, null))
                            .withLocation(addonAndModuleKey(PLUGIN_KEY,HEADER_WEB_ITEM_ID))
                            .withKey(WEB_SECTION_ID)
                            .build()
                )
                .addRoute("/pg", ConnectAppServlets.helloWorldServlet())
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
    public void testWebItemFoundWithinWebSection()
    {
        product.visit(JiraViewProjectPage.class, project.getKey());

        AddonDropdownMenu dropdown = connectPageOperations.getPageBinder().bind(AddonDropdownMenu.class, By.id(addonAndModuleKey(PLUGIN_KEY, HEADER_WEB_ITEM_ID)), By.id(addonAndModuleKey(PLUGIN_KEY, DROPDOWN_CONTENT_ID)));

        assertNotNull("Dropdown should be found", dropdown);

        dropdown.open();

        PageElement item = dropdown.getItem(By.id(addonAndModuleKey(PLUGIN_KEY, CONTENT_WEB_ITEM_ID)));

        assertNotNull("Web item within web section should be found", item);
        assertTrue("Web item url within web section should be correct", item.getAttribute("href").contains(CONTENT_WEB_ITEM_URL));
        assertEquals("Web item text within web section should be correct", CONTENT_WEB_ITEM_NAME, item.getText());
    }

    public static class AddonDropdownMenu extends JiraAuiDropdownMenu
    {
        public AddonDropdownMenu(final By triggerLocator, final By dropdownLocator)
        {
            super(triggerLocator, dropdownLocator);
        }

        public PageElement getItem(By selector)
        {
            return getDropdown().find(selector);
        }

        @Override
        protected PageElement trigger()
        {
            PageElement element = super.trigger();
            
            waitUntilTrue(element.timed().isVisible());
            
            return element;
        }
    }
}
